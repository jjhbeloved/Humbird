package org.humbird.soa.component.esign.tools;

import org.apache.camel.Endpoint;
import org.apache.camel.component.file.GenericFileConfiguration;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.component.file.remote.RemoteFileConfiguration;
import org.humbird.soa.common.net.ftp.FTPCallback;

import java.io.File;
import java.util.List;

/**
 * Created by david on 15/4/7.
 */
public abstract interface IFileOperations
{
    public abstract boolean retrieveFile(String paramString)
            throws GenericFileOperationFailedException;

    public abstract boolean retrieveFile(String paramString, File paramFile)
            throws GenericFileOperationFailedException;

    public abstract boolean storeFile(File paramFile)
            throws GenericFileOperationFailedException;

    public abstract boolean connection(RemoteFileConfiguration paramRemoteFileConfiguration)
            throws GenericFileOperationFailedException;

    public abstract void disconnection()
            throws GenericFileOperationFailedException;

    public abstract List displayFiles()
            throws GenericFileOperationFailedException;

    public abstract boolean removeFile(String paramString)
            throws GenericFileOperationFailedException;

    public abstract void jumpCurrentDirectory(String paramString)
            throws GenericFileOperationFailedException;

    public abstract boolean isConnected2();

    public abstract String getUserDirectory();

    public abstract void setUserDirectory(String paramString);

    public abstract void setEndpointn(Endpoint paramEndpoint);

    public abstract Endpoint getEndpointn();

    public abstract String getCurrentDir();

    public abstract RemoteFileConfiguration getRemoteFileConfiguration();

    public abstract Object getOperations();

    public abstract void setConfiguration(GenericFileConfiguration paramGenericFileConfiguration);

    public abstract void doAny(FTPCallback paramFTPCallback);

    public abstract int list(Object paramObject)
            throws Exception;

    public abstract Object getClient();
}