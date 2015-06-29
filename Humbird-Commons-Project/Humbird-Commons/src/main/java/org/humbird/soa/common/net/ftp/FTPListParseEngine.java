/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.humbird.soa.common.net.ftp;

import org.humbird.soa.common.net.util.Charsets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


/**
 * This class handles the entire process of parsing a listing of
 * file entries from the server.
 * <p>
 * This object defines a two-part parsing mechanism.
 * <p>
 * The first part is comprised of reading the raw input into an internal
 * list of strings.  Every item in this list corresponds to an actual
 * file.  All extraneous matter emitted by the server will have been
 * removed by the end of this phase.  This is accomplished in conjunction
 * with the FTPFileEntryParser associated with this engine, by calling
 * its methods <code>readNextEntry()</code> - which handles the issue of
 * what delimits one entry from another, usually but not always a line
 * feed and <code>preParse()</code> - which handles removal of
 * extraneous matter such as the preliminary lines of a listing, removal
 * of duplicates on versioning systems, etc.
 * <p>
 * The second part is composed of the actual parsing, again in conjunction
 * with the particular parser used by this engine.  This is controlled
 * by an iterator over the internal list of strings.  This may be done
 * either in block mode, by calling the <code>getNext()</code> and
 * <code>getPrevious()</code> methods to provide "paged" output of less
 * than the whole list at one time, or by calling the
 * <code>getFiles()</code> method to return the entire list.
 * <p>
 * Examples:
 * <p>
 * Paged access:
 * <pre>
 *    FTPClient f=FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPListParseEngine engine = f.initiateListParsing(directory);
 *
 *    while (engine.hasNext()) {
 *       FTPFile[] files = engine.getNext(25);  // "page size" you want
 *       //do whatever you want with these files, display them, etc.
 *       //expensive FTPFile objects not created until needed.
 *    }
 * </pre>
 * <p>
 * For unpaged access, simply use FTPClient.listFiles().  That method
 * uses this class transparently.
 * @version $Id: FTPListParseEngine.java 1414510 2012-11-28 02:40:39Z ggregory $
 */
public class FTPListParseEngine {
    private List<String> entries = new LinkedList<String>();
    private ListIterator<String> _internalIterator = entries.listIterator();

    private final org.humbird.soa.common.net.ftp.FTPFileEntryParser parser;

    public FTPListParseEngine(org.humbird.soa.common.net.ftp.FTPFileEntryParser parser) {
        this.parser = parser;
    }

    /**
     * handle the initial reading and preparsing of the list returned by
     * the server.  After this method has completed, this object will contain
     * a list of unparsed entries (Strings) each referring to a unique file
     * on the server.
     *
     * @param stream input stream provided by the server socket.
     * @param encoding the encoding to be used for reading the stream
     *
     * @exception java.io.IOException
     *                   thrown on any failure to read from the sever.
     */
    public void readServerList(InputStream stream, String encoding)
    throws IOException
    {
        this.entries = new LinkedList<String>();
        readStream(stream, encoding);
        this.parser.preParse(this.entries);
        resetIterator();
    }

    /**
     * Internal method for reading the input into the <code>entries</code> list.
     * After this method has completed, <code>entries</code> will contain a
     * collection of entries (as defined by
     * <code>FTPFileEntryParser.readNextEntry()</code>), but this may contain
     * various non-entry preliminary lines from the server output, duplicates,
     * and other data that will not be part of the final listing.
     *
     * @param stream The socket stream on which the input will be read.
     * @param encoding The encoding to use.
     *
     * @exception java.io.IOException
     *                   thrown on any failure to read the stream
     */
    private void readStream(InputStream stream, String encoding) throws IOException
    {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, Charsets.toCharset(encoding)));

        String line = this.parser.readNextEntry(reader);

        while (line != null)
        {
            this.entries.add(line);
            line = this.parser.readNextEntry(reader);
        }
        reader.close();
    }

    /**
     * handle the initial reading and preparsing of the list returned by
     * the server.  After this method has completed, this object will contain
     * a list of unparsed entries (Strings) each referring to a unique file
     * on the server.
     *
     * @param stream input stream provided by the server socket.
     * @param encoding the encoding to be used for reading the stream
     * @param callback the callback provided by developer
     *
     * @exception java.io.IOException
     *                   thrown on any failure to read from the sever.
     */
    public int readServerCallback(InputStream stream, String encoding, FTPFileFilter filter, FTPCallback callback)
            throws IOException
    {
//        this.entries = new LinkedList<String>();
        return filter == null ? readStream(stream, encoding, callback) : readStream(stream, encoding, filter, callback);
//        this.parser.preParse(this.entries);
    }

    /**
     * Internal method for reading the input into the <code>entries</code> list.
     * After this method has completed, <code>entries</code> will contain a
     * collection of entries (as defined by
     * <code>FTPFileEntryParser.readNextEntry()</code>), but this may contain
     * various non-entry preliminary lines from the server output, duplicates,
     * and other data that will not be part of the final listing.
     *
     * @param stream The socket stream on which the input will be read.
     * @param encoding The encoding to use.
     *
     * @exception java.io.IOException
     *                   thrown on any failure to read the stream
     */
    private int readStream(InputStream stream, String encoding, FTPCallback callback) throws IOException
    {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, Charsets.toCharset(encoding)));

        String line = this.parser.readNextEntry(reader);
        int count = 0;
        while (line != null)
        {
            List<String> lines = new ArrayList<String>();
            lines.add(line);
            if(this.parser.preParse(lines).size() > 0) {
                count += (0xff ^ (0xfe | callback.select(this.parser.parseFTPEntry(line))));
                lines.clear();
            }
            line = this.parser.readNextEntry(reader);
        }
        reader.close();
        return count;
    }

    /**
     * Internal method for reading the input into the <code>entries</code> list.
     * After this method has completed, <code>entries</code> will contain a
     * collection of entries (as defined by
     * <code>FTPFileEntryParser.readNextEntry()</code>), but this may contain
     * various non-entry preliminary lines from the server output, duplicates,
     * and other data that will not be part of the final listing.
     *
     * @param stream The socket stream on which the input will be read.
     * @param encoding The encoding to use.
     *
     * @exception java.io.IOException
     *                   thrown on any failure to read the stream
     */
    private int readStream(InputStream stream, String encoding, FTPFileFilter filter, FTPCallback callback) throws IOException
    {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, Charsets.toCharset(encoding)));

        String line = this.parser.readNextEntry(reader);
        int count = 0;
        while (line != null)
        {
            List<String> lines = new ArrayList<String>();
            lines.add(line);
            if(this.parser.preParse(lines).size() > 0) {
                FTPFile ftpFile = this.parser.parseFTPEntry(line);
                if (filter.accept(ftpFile)) {
                    count += (0xff ^ (0xfe | callback.select(this.parser.parseFTPEntry(line))));
                }
                lines.clear();
            }
            line = this.parser.readNextEntry(reader);
        }
        reader.close();
        return count;
    }

    /**
     * Returns an array of at most <code>quantityRequested</code> FTPFile
     * objects starting at this object's internal iterator's current position.
     * If fewer than <code>quantityRequested</code> such
     * elements are available, the returned array will have a length equal
     * to the number of entries at and after after the current position.
     * If no such entries are found, this array will have a length of 0.
     *
     * After this method is called this object's internal iterator is advanced
     * by a number of positions equal to the size of the array returned.
     *
     * @param quantityRequested
     * the maximum number of entries we want to get.
     *
     * @return an array of at most <code>quantityRequested</code> FTPFile
     * objects starting at the current position of this iterator within its
     * list and at least the number of elements which  exist in the list at
     * and after its current position.
     * <p><b>
     * NOTE:</b> This array may contain null members if any of the
     * individual file listings failed to parse.  The caller should
     * check each entry for null before referencing it.
     */
    public org.humbird.soa.common.net.ftp.FTPFile[] getNext(int quantityRequested) {
        List<org.humbird.soa.common.net.ftp.FTPFile> tmpResults = new LinkedList<org.humbird.soa.common.net.ftp.FTPFile>();
        int count = quantityRequested;
        while (count > 0 && this._internalIterator.hasNext()) {
            String entry = this._internalIterator.next();
            org.humbird.soa.common.net.ftp.FTPFile temp = this.parser.parseFTPEntry(entry);
            tmpResults.add(temp);
            count--;
        }
        return tmpResults.toArray(new org.humbird.soa.common.net.ftp.FTPFile[tmpResults.size()]);

    }

    /**
     * Returns an array of at most <code>quantityRequested</code> FTPFile
     * objects starting at this object's internal iterator's current position,
     * and working back toward the beginning.
     *
     * If fewer than <code>quantityRequested</code> such
     * elements are available, the returned array will have a length equal
     * to the number of entries at and after after the current position.
     * If no such entries are found, this array will have a length of 0.
     *
     * After this method is called this object's internal iterator is moved
     * back by a number of positions equal to the size of the array returned.
     *
     * @param quantityRequested
     * the maximum number of entries we want to get.
     *
     * @return an array of at most <code>quantityRequested</code> FTPFile
     * objects starting at the current position of this iterator within its
     * list and at least the number of elements which  exist in the list at
     * and after its current position.  This array will be in the same order
     * as the underlying list (not reversed).
     * <p><b>
     * NOTE:</b> This array may contain null members if any of the
     * individual file listings failed to parse.  The caller should
     * check each entry for null before referencing it.
     */
    public org.humbird.soa.common.net.ftp.FTPFile[] getPrevious(int quantityRequested) {
        List<org.humbird.soa.common.net.ftp.FTPFile> tmpResults = new LinkedList<org.humbird.soa.common.net.ftp.FTPFile>();
        int count = quantityRequested;
        while (count > 0 && this._internalIterator.hasPrevious()) {
            String entry = this._internalIterator.previous();
            org.humbird.soa.common.net.ftp.FTPFile temp = this.parser.parseFTPEntry(entry);
            tmpResults.add(0,temp);
            count--;
        }
        return tmpResults.toArray(new org.humbird.soa.common.net.ftp.FTPFile[tmpResults.size()]);
    }

    /**
     * Returns an array of FTPFile objects containing the whole list of
     * files returned by the server as read by this object's parser.
     *
     * @return an array of FTPFile objects containing the whole list of
     *         files returned by the server as read by this object's parser.
     * None of the entries will be null
     * @exception java.io.IOException - not ever thrown, may be removed in a later release
     */
    public FTPFile[] getFiles()
    throws IOException // TODO remove; not actually thrown
    {
        return getFiles(org.humbird.soa.common.net.ftp.FTPFileFilters.NON_NULL);
    }

    /**
     * Returns an array of FTPFile objects containing the whole list of
     * files returned by the server as read by this object's parser.
     * The files are filtered before being added to the array.
     *
     * @param filter FTPFileFilter, must not be <code>null</code>.
     *
     * @return an array of FTPFile objects containing the whole list of
     *         files returned by the server as read by this object's parser.
     * <p><b>
     * NOTE:</b> This array may contain null members if any of the
     * individual file listings failed to parse.  The caller should
     * check each entry for null before referencing it, or use the
     * a filter such as {@link org.humbird.soa.common.net.ftp.FTPFileFilters#NON_NULL} which does not
     * allow null entries.
     * @since 2.2
     * @exception java.io.IOException - not ever thrown, may be removed in a later release
     */
    public FTPFile[] getFiles(FTPFileFilter filter)
    throws IOException // TODO remove; not actually thrown
    {
        List<org.humbird.soa.common.net.ftp.FTPFile> tmpResults = new ArrayList<org.humbird.soa.common.net.ftp.FTPFile>();
        Iterator<String> iter = this.entries.iterator();
        while (iter.hasNext()) {
            String entry = iter.next();
            org.humbird.soa.common.net.ftp.FTPFile temp = this.parser.parseFTPEntry(entry);
            if (filter.accept(temp)){
                tmpResults.add(temp);
            }
        }
        return tmpResults.toArray(new FTPFile[tmpResults.size()]);

    }

    public int getFiles(FTPCallback ftpCallback)
            throws IOException // TODO remove; not actually thrown
    {
        List<FTPFile> tmpResults = new ArrayList<FTPFile>();
        Iterator<String> iter = this.entries.iterator();
        int count = 0;
        while (iter.hasNext()) {
            String entry = iter.next();
            FTPFile temp = this.parser.parseFTPEntry(entry);
            count += ftpCallback.select(temp);
        }
        return count;
    }

    public int getFiles(FTPFileFilter filter, FTPCallback ftpCallback)
            throws IOException // TODO remove; not actually thrown
    {
        List<FTPFile> tmpResults = new ArrayList<FTPFile>();
        Iterator<String> iter = this.entries.iterator();
        int count = 0;
        while (iter.hasNext()) {
            String entry = iter.next();
            FTPFile temp = this.parser.parseFTPEntry(entry);
            if (filter.accept(temp)){
                count += ftpCallback.select(temp);
            }
        }
        return count;
    }

    /**
     * convenience method to allow clients to know whether this object's
     * internal iterator's current position is at the end of the list.
     *
     * @return true if internal iterator is not at end of list, false
     * otherwise.
     */
    public boolean hasNext() {
        return _internalIterator.hasNext();
    }

    /**
     * convenience method to allow clients to know whether this object's
     * internal iterator's current position is at the beginning of the list.
     *
     * @return true if internal iterator is not at beginning of list, false
     * otherwise.
     */
    public boolean hasPrevious() {
        return _internalIterator.hasPrevious();
    }

    /**
     * resets this object's internal iterator to the beginning of the list.
     */
    public void resetIterator() {
        this._internalIterator = this.entries.listIterator();
    }

    // DEPRECATED METHODS - for API compatibility only - DO NOT USE

    /**
     * Do not use.
     * @deprecated use {@link #readServerList(java.io.InputStream, String)} instead
    */
    @Deprecated
    public void readServerList(InputStream stream)
    throws IOException
    {
        readServerList(stream, null);
    }

}
