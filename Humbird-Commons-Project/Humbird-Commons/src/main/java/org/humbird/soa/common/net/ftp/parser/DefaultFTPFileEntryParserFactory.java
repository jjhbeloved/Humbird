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

package org.humbird.soa.common.net.ftp.parser;

import org.humbird.soa.common.net.ftp.Configurable;
import org.humbird.soa.common.net.ftp.FTPClientConfigX;
import org.humbird.soa.common.net.ftp.FTPFileEntryParser;

import java.util.regex.Pattern;


/**
 * This is the default implementation of the
 * FTPFileEntryParserFactory interface.  This is the
 * implementation that will be used by
 * org.humbird.soa.common.net.FTPClient.listFiles()
 * if no other implementation has been specified.
 *
 * @see org.humbird.soa.common.net.ftp.FTPClientX#listFiles
 * @see org.humbird.soa.common.net.ftp.FTPClientX#setParserFactory
 */
public class DefaultFTPFileEntryParserFactory
    implements FTPFileEntryParserFactory
{

    // Match a plain Java Identifier
    private static final String JAVA_IDENTIFIER = "\\p{javaJavaIdentifierStart}(\\p{javaJavaIdentifierPart})*";
    // Match a qualified name, e.g. a.b.c.Name - but don't allow the default package as that would allow "VMS"/"UNIX" etc.
    private static final String JAVA_QUALIFIED_NAME  = "("+JAVA_IDENTIFIER+"\\.)+"+JAVA_IDENTIFIER;
    // Create the pattern, as it will be reused many times
    private static final Pattern JAVA_QUALIFIED_NAME_PATTERN = Pattern.compile(JAVA_QUALIFIED_NAME);

    /**
     * This default implementation of the FTPFileEntryParserFactory
     * interface works according to the following logic:
     * First it attempts to interpret the supplied key as a fully
     * qualified classname (default package is not allowed) of a class implementing the
     * FTPFileEntryParser interface.  If that succeeds, a parser
     * object of this class is instantiated and is returned;
     * otherwise it attempts to interpret the key as an identirier
     * commonly used by the FTP SYST command to identify systems.
     * <p/>
     * If <code>key</code> is not recognized as a fully qualified
     * classname known to the system, this method will then attempt
     * to see whether it <b>contains</b> a string identifying one of
     * the known parsers.  This comparison is <b>case-insensitive</b>.
     * The intent here is where possible, to select as keys strings
     * which are returned by the SYST command on the systems which
     * the corresponding parser successfully parses.  This enables
     * this factory to be used in the auto-detection system.
     * <p/>
     *
     * @param key    should be a fully qualified classname corresponding to
     *               a class implementing the FTPFileEntryParser interface<br/>
     *               OR<br/>
     *               a string containing (case-insensitively) one of the
     *               following keywords:
     *               <ul>
     *               <li>{@link org.humbird.soa.common.net.ftp.FTPClientConfigX#SYST_UNIX UNIX}</li>
     *               <li>{@link org.humbird.soa.common.net.ftp.FTPClientConfigX#SYST_NT WINDOWS}</li>
     *               <li>{@link org.humbird.soa.common.net.ftp.FTPClientConfigX#SYST_OS2 OS/2}</li>
     *               <li>{@link org.humbird.soa.common.net.ftp.FTPClientConfigX#SYST_OS400 OS/400}</li>
     *               <li>{@link org.humbird.soa.common.net.ftp.FTPClientConfigX#SYST_AS400 AS/400}</li>
     *               <li>{@link org.humbird.soa.common.net.ftp.FTPClientConfigX#SYST_VMS VMS}</li>
     *               <li>{@link org.humbird.soa.common.net.ftp.FTPClientConfigX#SYST_MVS MVS}</li>
     *               <li>{@link org.humbird.soa.common.net.ftp.FTPClientConfigX#SYST_NETWARE NETWARE}</li>
     *               <li>{@link org.humbird.soa.common.net.ftp.FTPClientConfigX#SYST_L8 TYPE:L8}</li>
     *               </ul>
     * @return the FTPFileEntryParser corresponding to the supplied key.
     * @throws org.humbird.soa.common.net.ftp.parser.ParserInitializationException thrown if for any reason the factory cannot resolve
     *                   the supplied key into an FTPFileEntryParser.
     * @see org.humbird.soa.common.net.ftp.FTPFileEntryParser
     */
//    @Override
    public FTPFileEntryParser createFileEntryParser(String key)
    {
        if (key == null) {
            throw new org.humbird.soa.common.net.ftp.parser.ParserInitializationException("Parser key cannot be null");
        }
        return createFileEntryParser(key, null);
    }

    // Common method to process both key and config parameters.
    private FTPFileEntryParser createFileEntryParser(String key, FTPClientConfigX config) {
        FTPFileEntryParser parser = null;

        // Is the key a possible class name?
        if (JAVA_QUALIFIED_NAME_PATTERN.matcher(key).matches()) {
            try
            {
                Class<?> parserClass = Class.forName(key);
                try {
                    parser = (FTPFileEntryParser) parserClass.newInstance();
                } catch (ClassCastException e) {
                    throw new org.humbird.soa.common.net.ftp.parser.ParserInitializationException(parserClass.getName()
                        + " does not implement the interface "
                        + "org.humbird.soa.common.net.ftp.FTPFileEntryParser.", e);
                } catch (Exception e) {
                    throw new org.humbird.soa.common.net.ftp.parser.ParserInitializationException("Error initializing parser", e);
                } catch (ExceptionInInitializerError e) {
                    throw new org.humbird.soa.common.net.ftp.parser.ParserInitializationException("Error initializing parser", e);
                }
            } catch (ClassNotFoundException e) {
                // OK, assume it is an alias
            }
        }

        if (parser == null) { // Now try for aliases
            String ukey = key.toUpperCase(java.util.Locale.ENGLISH);
            if (ukey.indexOf(FTPClientConfigX.SYST_UNIX) >= 0)
            {
                parser = new org.humbird.soa.common.net.ftp.parser.UnixFTPEntryParser(config);
            }
            else if (ukey.indexOf(FTPClientConfigX.SYST_VMS) >= 0)
            {
                parser = new org.humbird.soa.common.net.ftp.parser.VMSVersioningFTPEntryParser(config);
            }
            else if (ukey.indexOf(FTPClientConfigX.SYST_NT) >= 0)
            {
                parser = createNTFTPEntryParser(config);
            }
            else if (ukey.indexOf(FTPClientConfigX.SYST_OS2) >= 0)
            {
                parser = new org.humbird.soa.common.net.ftp.parser.OS2FTPEntryParser(config);
            }
            else if (ukey.indexOf(FTPClientConfigX.SYST_OS400) >= 0 ||
                    ukey.indexOf(FTPClientConfigX.SYST_AS400) >= 0)
            {
                parser = createOS400FTPEntryParser(config);
            }
            else if (ukey.indexOf(FTPClientConfigX.SYST_MVS) >= 0)
            {
                parser = new MVSFTPEntryParser(); // Does not currently support config parameter
            }
            else if (ukey.indexOf(FTPClientConfigX.SYST_NETWARE) >= 0)
            {
                parser = new org.humbird.soa.common.net.ftp.parser.NetwareFTPEntryParser(config);
            }
            else if (ukey.indexOf(FTPClientConfigX.SYST_MACOS_PETER) >= 0)
            {
                parser = new MacOsPeterFTPEntryParser(config);
            }
            else if (ukey.indexOf(FTPClientConfigX.SYST_L8) >= 0)
            {
                // L8 normally means Unix, but move it to the end for some L8 systems that aren't.
                // This check should be last!
                parser = new org.humbird.soa.common.net.ftp.parser.UnixFTPEntryParser(config);
            }
            else
            {
                throw new org.humbird.soa.common.net.ftp.parser.ParserInitializationException("Unknown parser type: " + key);
            }
        }

        if (parser instanceof Configurable) {
            ((Configurable)parser).configure(config);
        }
        return parser;
    }

    /**
     * <p>Implementation extracts a key from the supplied
     * {@link  org.humbird.soa.common.net.ftp.FTPClientConfigX FTPClientConfigX}
     * parameter and creates an object implementing the
     * interface FTPFileEntryParser and uses the supplied configuration
     * to configure it.
     * </p><p>
     * Note that this method will generally not be called in scenarios
     * that call for autodetection of parser type but rather, for situations
     * where the user knows that the server uses a non-default configuration
     * and knows what that configuration is.
     * </p>
     * @param config  A {@link  org.humbird.soa.common.net.ftp.FTPClientConfigX FTPClientConfigX}
     * used to configure the parser created
     *
     * @return the @link  FTPFileEntryParser FTPFileEntryParser} so created.
     * @exception org.humbird.soa.common.net.ftp.parser.ParserInitializationException
     *                   Thrown on any exception in instantiation
     * @throws NullPointerException if {@code config} is {@code null}
     * @since 1.4
     */
//    @Override
    public FTPFileEntryParser createFileEntryParser(FTPClientConfigX config)
    throws ParserInitializationException
    {
        String key = config.getServerSystemKey();
        return createFileEntryParser(key, config);
    }


    public FTPFileEntryParser createUnixFTPEntryParser()
    {
        return new UnixFTPEntryParser();
    }

    public FTPFileEntryParser createVMSVersioningFTPEntryParser()
    {
        return new VMSVersioningFTPEntryParser();
    }

    public FTPFileEntryParser createNetwareFTPEntryParser() {
        return new NetwareFTPEntryParser();
    }

    public FTPFileEntryParser createNTFTPEntryParser()
    {
        return createNTFTPEntryParser(null);
    }

    /**
     * Creates an NT FTP parser: if the config exists, and the system key equals
     * {@link org.humbird.soa.common.net.ftp.FTPClientConfigX.SYST_NT} then a plain {@link org.humbird.soa.common.net.ftp.parser.NTFTPEntryParser} is used,
     * otherwise a composite of {@link org.humbird.soa.common.net.ftp.parser.NTFTPEntryParser} and {@link org.humbird.soa.common.net.ftp.parser.UnixFTPEntryParser} is used.
     * @param config the config to use, may be {@code null}
     * @return the parser
     */
    private FTPFileEntryParser createNTFTPEntryParser(FTPClientConfigX config)
    {
        if (config != null && FTPClientConfigX.SYST_NT.equals(
                config.getServerSystemKey()))
        {
            return new NTFTPEntryParser(config);
        } else {
            return new CompositeFileEntryParser(new FTPFileEntryParser[]
                   {
                       new NTFTPEntryParser(config),
                       new UnixFTPEntryParser(config)
                   });
        }
    }

     public FTPFileEntryParser createOS2FTPEntryParser()
    {
        return new OS2FTPEntryParser();
    }

    public FTPFileEntryParser createOS400FTPEntryParser()
    {
        return createOS400FTPEntryParser(null);
    }

    /**
     * Creates an OS400 FTP parser: if the config exists, and the system key equals
     * {@link org.humbird.soa.common.net.ftp.FTPClientConfigX.SYST_OS400} then a plain {@link org.humbird.soa.common.net.ftp.parser.OS400FTPEntryParser} is used,
     * otherwise a composite of {@link org.humbird.soa.common.net.ftp.parser.OS400FTPEntryParser} and {@link org.humbird.soa.common.net.ftp.parser.UnixFTPEntryParser} is used.
     * @param config the config to use, may be {@code null}
     * @return the parser
     */
    private FTPFileEntryParser createOS400FTPEntryParser(FTPClientConfigX config)
        {
        if (config != null &&
                FTPClientConfigX.SYST_OS400.equals(config.getServerSystemKey()))
        {
            return new org.humbird.soa.common.net.ftp.parser.OS400FTPEntryParser(config);
        } else {
            return new CompositeFileEntryParser(new FTPFileEntryParser[]
                {
                    new OS400FTPEntryParser(config),
                    new UnixFTPEntryParser(config)
                });
        }
    }

    public FTPFileEntryParser createMVSEntryParser()
    {
        return new MVSFTPEntryParser();
    }

}

