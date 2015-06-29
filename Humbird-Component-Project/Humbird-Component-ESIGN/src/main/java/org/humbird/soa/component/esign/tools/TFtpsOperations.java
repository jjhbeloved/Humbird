package org.humbird.soa.component.esign.tools;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFileConfiguration;
import org.apache.camel.component.file.GenericFileExist;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.component.file.remote.*;
import org.apache.camel.util.*;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.humbird.soa.common.net.ftp.FTPCallback;

import java.io.*;
import java.util.List;

/**
 * Created by david on 15/4/7.
 */
public class TFtpsOperations extends FtpsOperations
        implements IFileOperations
{
    private TFtpsOperations tFtpsOperations;
    private FtpsEndpoint ftpsEndpoint;
    protected FTPSClient ftpsClient;
    protected FTPClientConfig ftpsClientConfig;
    private String userDirectory;
    protected GenericFileConfiguration configuration;
    private CamelContext camelContext;
    private RemoteFileOperations remoteFileOperations;

    public TFtpsOperations()
    {
        super(null, null);
    }

    public TFtpsOperations(FTPSClient client, FTPClientConfig clientConfig) {
        super(client, clientConfig);
        this.ftpsClient = client;
        this.ftpsClientConfig = clientConfig;
    }

    public TFtpsOperations createTFtpsOperations(RemoteFileOperations remoteFileOperations) {
        this.remoteFileOperations = remoteFileOperations;
        return this;
    }
    public boolean retrieveFile(String name) throws GenericFileOperationFailedException {
        File local = new File(FileUtil.normalizePath(this.ftpsEndpoint.getLocalWorkDirectory()));
        File temp;
        File lock;
        FileOutputStream os;
        try { String e = name;
            temp = new File(local, e + ".inprogress");
            lock = new File(local, e + ".lock");
            local = new File(local, e);
            if ((lock.exists()) || (temp.exists())) {
                return false;
            }
            if (local.exists()) {
                if (!lock.createNewFile()) {
                    return false;
                }
                return true;
            }

            if (!temp.createNewFile()) {
                return false;
            }

            os = new FileOutputStream(temp);
        } catch (Exception var19) {
            throw new GenericFileOperationFailedException("Cannot create new local work file: " + local);
        }
        boolean result1;
        try
        {
            String deleted1 = name;
            String currentDir = null;
            if (this.ftpsEndpoint.getConfiguration().isStepwise()) {
                currentDir = getCurrentDir();
                String path = FileUtil.onlyPath(name);
                if (path != null) {
                    changeCurrentDirectory(path);
                }

                deleted1 = FileUtil.stripPath(name);
            }

            this.log.trace("Client retrieveFile: {}", deleted1);
            try {
                FileUtil.createNewFile(lock);
            } catch (IOException e1) {
                return false;
            }
            result1 = this.client.retrieveFile(deleted1, os);
            if ((result1) &&
                    (this.ftpsEndpoint.isDelete())) {
                try {
                    deleteFile(deleted1);
                }
                catch (GenericFileOperationFailedException gofe)
                {
                }
            }
            if (this.ftpsEndpoint.getConfiguration().isStepwise())
                changeCurrentDirectory(currentDir);
        }
        catch (IOException var17) {
            this.log.trace("Error occurred during retrieving file: {} to local directory. Deleting local work file: {}", name, temp);
            IOHelper.close(os, "retrieve: " + name, this.log);
            boolean deleted = FileUtil.deleteFile(temp);
            FileUtil.deleteFile(lock);
            if (!deleted) {
                this.log.warn("Error occurred during retrieving file: " + name + " to local directory. Cannot delete local work file: " + temp);
            }

            throw new GenericFileOperationFailedException(this.client.getReplyCode(), this.client.getReplyString(), var17.getMessage(), var17);
        } finally {
            IOHelper.close(os, "retrieve: " + name, this.log);
        }

        this.log.debug("Retrieve file to local work file result: {}", Boolean.valueOf(result1));
        if (result1) {
            this.log.trace("Renaming local in progress file from: {} to: {}", temp, local);
            try {
                if (!FileUtil.renameFile(temp, local, false)) {
                    FileUtil.deleteFile(lock);
                    return false;
                }
            } catch (IOException var16) {
                throw new GenericFileOperationFailedException("Cannot rename local work file from: " + temp + " to: " + local, var16);
            }
        }
        return result1;
    }
    public boolean retrieveFile(String name, File local) throws GenericFileOperationFailedException {
        File temp;
        File lock;
        FileOutputStream os;
        try {
            String e = name;
            temp = new File(local, e + ".inprogress");
            lock = new File(local, e + ".lock");
            local = new File(local, e);
            if ((lock.exists()) || (temp.exists())) {
                return false;
            }
            if (local.exists()) {
                if (!lock.createNewFile()) {
                    return false;
                }
                return true;
            }

            if (!temp.createNewFile()) {
                return false;
            }

            os = new FileOutputStream(temp);
        } catch (Exception var19) {
            throw new GenericFileOperationFailedException("Cannot create new local work file: " + local);
        }
        boolean result1;
        try
        {
            String deleted1 = name;
            String currentDir = null;
            if (this.ftpsEndpoint.getConfiguration().isStepwise()) {
                currentDir = getCurrentDir();
                String path = FileUtil.onlyPath(name);
                if (path != null) {
                    changeCurrentDirectory(path);
                }

                deleted1 = FileUtil.stripPath(name);
            }

            this.log.trace("Client retrieveFile: {}", deleted1);
            try {
                FileUtil.createNewFile(lock);
            } catch (IOException e1) {
                return false;
            }
            result1 = this.client.retrieveFile(deleted1, os);
            if ((result1) &&
                    (this.ftpsEndpoint.isDelete())) {
                try {
                    deleteFile(deleted1);
                }
                catch (GenericFileOperationFailedException gofe)
                {
                }
            }
            if (this.ftpsEndpoint.getConfiguration().isStepwise())
                changeCurrentDirectory(currentDir);
        }
        catch (IOException var17) {
            this.log.trace("Error occurred during retrieving file: {} to local directory. Deleting local work file: {}", name, temp);
            IOHelper.close(os, "retrieve: " + name, this.log);
            boolean deleted = FileUtil.deleteFile(temp);
            FileUtil.deleteFile(lock);
            if (!deleted) {
                this.log.warn("Error occurred during retrieving file: " + name + " to local directory. Cannot delete local work file: " + temp);
            }

            throw new GenericFileOperationFailedException(this.client.getReplyCode(), this.client.getReplyString(), var17.getMessage(), var17);
        } finally {
            IOHelper.close(os, "retrieve: " + name, this.log);
        }

        this.log.debug("Retrieve file to local work file result: {}", Boolean.valueOf(result1));
        if (result1) {
            this.log.trace("Renaming local in progress file from: {} to: {}", temp, local);
            try {
                if (!FileUtil.renameFile(temp, local, false)) {
                    FileUtil.deleteFile(lock);
                    return false;
                }
            } catch (IOException var16) {
                throw new GenericFileOperationFailedException("Cannot rename local work file from: " + temp + " to: " + local, var16);
            }
        }
        return result1;
    }

    public boolean storeFile(File _f_) throws GenericFileOperationFailedException
    {
        FileInputStream is = null;
        try {
            is = new FileInputStream(_f_);
            int size = is.available();
            if (size == 0) {
                _f_.delete();
                IOHelper.close(is, "store: " + _f_.getName(), this.log);
                return false;
            }
        }
        catch (FileNotFoundException e) {
            IOHelper.close(is, "retrieve: " + _f_.getName(), this.log);
            return false;
        }
        catch (IOException e) {
            IOHelper.close(is, "retrieve: " + _f_.getName(), this.log);
            return false;
        }
        String name = this.ftpsEndpoint.getConfiguration().normalizePath(_f_.getName());
        this.log.trace("storeFile({})", name);
        boolean answer = false;
        String currentDir = null;
        String path = FileUtil.onlyPath(name);
        String targetName = name;
        try
        {
            if ((path != null) && (this.ftpsEndpoint.getConfiguration().isStepwise())) {
                currentDir = getCurrentDir();
                changeCurrentDirectory(path);
                targetName = FileUtil.stripPath(name);
            }
            answer = doStoreFile(name, targetName, is);
        } finally {
            IOHelper.close(is, "retrieve: " + _f_.getName(), this.log);
            if (currentDir != null) {
                changeCurrentDirectory(currentDir);
            }
            if ((answer) &&
                    (this.ftpsEndpoint.isDelete())) {
                _f_.delete();
            }

        }

        return answer;
    }

    private boolean doStoreFile(String name, String targetName, InputStream is1)
            throws GenericFileOperationFailedException
    {
        this.log.trace("doStoreFile({})", targetName);
        if ((this.ftpsEndpoint.getFileExist() == GenericFileExist.Ignore) || (this.ftpsEndpoint.getFileExist() == GenericFileExist.Fail) || (this.ftpsEndpoint.getFileExist() == GenericFileExist.Move)) {
            boolean is = existsFile(targetName);
            if ((is) && (this.ftpsEndpoint.getFileExist() == GenericFileExist.Ignore)) {
                this.log.trace("An existing file already exists: {}. Ignore and do not override it.", name);
                return true;
            }

            if ((is) && (this.ftpsEndpoint.getFileExist() == GenericFileExist.Fail)) {
                throw new GenericFileOperationFailedException("File already exist: " + name + ". Cannot write new file.");
            }

            if ((is) && (this.ftpsEndpoint.getFileExist() == GenericFileExist.Move)) {
                doMoveExistingFile(name, targetName);
            }
        }
        boolean var7;
        try
        {
            StopWatch e1 = new StopWatch();
            this.log.debug("About to store file: {} using stream: {}", targetName, is1);
            boolean answer;
            if (this.ftpsEndpoint.getFileExist() == GenericFileExist.Append) {
                this.log.trace("Client appendFile: {}", targetName);
                answer = this.client.appendFile(targetName, is1);
            } else {
                this.log.trace("Client storeFile: {}", targetName);
                answer = this.client.storeFile(targetName, is1);
            }

            e1.stop();
            if (this.log.isDebugEnabled()) {
                this.log.debug("Took {} ({} millis) to store file: {} and FTP client returned: {}", new Object[] { TimeUtils.printDuration(e1.taken()), Long.valueOf(e1.taken()), targetName, Boolean.valueOf(answer) });
            }
            var7 = answer;
        } catch (IOException var12) {
            throw new GenericFileOperationFailedException(this.client.getReplyCode(), this.client.getReplyString(), var12.getMessage(), var12);
        } finally {
            IOHelper.close(is1, "store: " + name, this.log);
        }

        return var7;
    }

    private void doMoveExistingFile(String name, String targetName) throws GenericFileOperationFailedException {
        Exchange dummy = this.ftpsEndpoint.createExchange();
        Object parent = null;
        String onlyName = FileUtil.stripPath(targetName);
        dummy.getIn().setHeader("CamelFileName", targetName);
        dummy.getIn().setHeader("CamelFileNameOnly", onlyName);
        dummy.getIn().setHeader("CamelFileParent", parent);
        String to = (String)this.ftpsEndpoint.getMoveExisting().evaluate(dummy, String.class);
        to = FileUtil.stripLeadingSeparator(to);
        to = this.ftpsEndpoint.getConfiguration().normalizePath(to);
        if (ObjectHelper.isEmpty(to)) {
            throw new GenericFileOperationFailedException("moveExisting evaluated as empty String, cannot move existing file: " + name);
        }
        String dir = FileUtil.onlyPath(to);
        if (dir != null) {
            buildDirectory(dir, false);
        }

        if (existsFile(to)) {
            if (!this.ftpsEndpoint.isEagerDeleteTargetFile()) {
                throw new GenericFileOperationFailedException("Cannot moved existing file from: " + name + " to: " + to + " as there already exists a file: " + to);
            }

            this.log.trace("Deleting existing file: {}", to);
            try
            {
                boolean result = this.client.deleteFile(to);
                if (!result)
                    throw new GenericFileOperationFailedException("Cannot delete file: " + to);
            }
            catch (IOException var10) {
                throw new GenericFileOperationFailedException(this.client.getReplyCode(), this.client.getReplyString(), "Cannot delete file: " + to, var10);
            }
        }

        this.log.trace("Moving existing file: {} to: {}", name, to);
        if (!renameFile(targetName, to))
            throw new GenericFileOperationFailedException("Cannot rename file from: " + name + " to: " + to);
    }

    public boolean connection(RemoteFileConfiguration configuration)
            throws GenericFileOperationFailedException
    {
        boolean answer = this.remoteFileOperations.connect(configuration);
        this.userDirectory = this.remoteFileOperations.getCurrentDirectory();
        return answer;
    }

    public void disconnection() throws GenericFileOperationFailedException
    {
        super.disconnect();
    }

    public List<FTPFile> displayFiles() throws GenericFileOperationFailedException
    {
        return this.remoteFileOperations.listFiles();
    }

    public boolean removeFile(String name) throws GenericFileOperationFailedException
    {
        return super.deleteFile(name);
    }

    public void jumpCurrentDirectory(String path) throws GenericFileOperationFailedException
    {
        this.remoteFileOperations.changeCurrentDirectory(path);
    }

    public boolean isConnected2()
    {
        return isConnected();
    }

    public String getUserDirectory()
    {
        return this.userDirectory;
    }

    public void setUserDirectory(String userDirectory)
    {
        this.userDirectory = userDirectory;
    }

    public String getCurrentDirectory() throws GenericFileOperationFailedException {
        return this.remoteFileOperations.getCurrentDirectory();
    }

    public boolean existsFile(String name) throws GenericFileOperationFailedException {
        return this.remoteFileOperations.existsFile(name);
    }

    protected FTPSClient createFtpsClient() throws Exception {
        return new FTPSClient();
    }

    public FTPSClient getFtpsClient() {
        return this.ftpsClient;
    }

    public FtpsConfiguration getFtpsConfiguration() {
        if (this.configuration == null) {
            this.configuration = new FtpsConfiguration();
        }
        return (FtpsConfiguration)this.configuration;
    }

    public TFtpsOperations gettFtpsOperations() {
        return this.tFtpsOperations;
    }

    public void settFtpsOperations(TFtpsOperations tFtpsOperations) {
        this.tFtpsOperations = tFtpsOperations;
    }

    public void setEndpointn(Endpoint endpoint)
    {
        this.ftpsEndpoint = ((FtpsEndpoint)endpoint);
        super.setEndpoint(this.ftpsEndpoint);
    }

    public Endpoint getEndpointn()
    {
        return this.ftpsEndpoint;
    }

    public String getCurrentDir()
    {
        return this.ftpsEndpoint.getConfiguration().getDirectory();
    }

    public RemoteFileConfiguration getRemoteFileConfiguration()
    {
        return this.ftpsEndpoint.getConfiguration();
    }

    public Object getOperations()
    {
        return this.tFtpsOperations;
    }

    public void setConfiguration(GenericFileConfiguration configuration)
    {
        if (configuration == null) {
            throw new IllegalArgumentException("FtpConfiguration expected");
        }
        this.configuration = configuration;
    }

    public void doAny(FTPCallback callback)
    {
    }

    public int list(Object callback)
            throws Exception
    {
        return 0;
    }

    public Object getClient()
    {
        return this.ftpsClient;
    }

    public FTPClientConfig getFtpClientConfig() {
        return this.ftpsClientConfig;
    }

    public CamelContext getCamelContext() {
        return this.camelContext;
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public RemoteFileOperations getRemoteFileOperations() {
        return this.remoteFileOperations;
    }

    public void setRemoteFileOperations(RemoteFileOperations remoteFileOperations) {
        this.remoteFileOperations = remoteFileOperations;
    }
}