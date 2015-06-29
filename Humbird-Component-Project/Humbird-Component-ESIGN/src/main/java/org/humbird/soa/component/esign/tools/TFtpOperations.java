package org.humbird.soa.component.esign.tools;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFileConfiguration;
import org.apache.camel.component.file.GenericFileExist;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.component.file.remote.*;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.*;
import org.humbird.soa.common.net.ftp.*;

import java.io.*;
import java.util.*;

/**
 * Created by david on 15/4/7.
 */
public class TFtpOperations extends FtpOperations
        implements IFileOperations
{
    protected FTPClientX ftpClient;
    protected FTPClientConfigX ftpClientConfig;
    protected Map<String, Object> ftpClientParameters;
    protected Map<String, Object> ftpClientConfigParameters;

    @UriParam
    protected int soTimeout;

    @UriParam
    protected int dataTimeout;
    protected GenericFileConfiguration configuration;
    private CamelContext camelContext;
    private String userDirectory;
    private TFtpOperations tFtpOperations;
    private FtpEndpoint ftpEndpoint;

    public TFtpOperations()
    {
        super(null, null);
    }

    public TFtpOperations(FTPClientX client, FTPClientConfigX clientConfig) {
        super(null, null);
        this.ftpClient = client;
        this.ftpClientConfig = clientConfig;
    }

    public TFtpOperations createTFtpOperations() throws Exception {
        if (this.ftpClient == null) {
            this.ftpClient = createFtpClient();
        }

        if (getConfiguration().getConnectTimeout() > -1) {
            this.ftpClient.setConnectTimeout(getConfiguration().getConnectTimeout());
        }

        if (getConfiguration().getSoTimeout() > -1) {
            this.soTimeout = getConfiguration().getSoTimeout();
        }

        this.dataTimeout = getConfiguration().getTimeout();

        if (this.ftpClientParameters != null) {
            HashMap operations = new HashMap(this.ftpClientParameters);
            Object timeout = operations.remove("soTimeout");
            if (timeout != null) {
                this.soTimeout = (getCamelContext().getTypeConverter().convertTo(Integer.TYPE, timeout)).intValue();
            }

            timeout = operations.remove("dataTimeout");
            if (timeout != null) {
                this.dataTimeout = (getCamelContext().getTypeConverter().convertTo(Integer.TYPE, Integer.valueOf(this.dataTimeout))).intValue();
            }

            setProperties(this.ftpClient, operations);
        }

        if (this.ftpClientConfigParameters != null) {
            if (this.ftpClientConfig == null) {
                this.ftpClientConfig = new FTPClientConfigX();
            }

            HashMap operations = new HashMap(this.ftpClientConfigParameters);
            setProperties(this.ftpClientConfig, operations);
        }

        if (this.dataTimeout > 0) {
            this.ftpClient.setDataTimeout(this.dataTimeout);
        }

        if (this.log.isDebugEnabled()) {
            this.log.debug("Created FTPClient [connectTimeout: {}, soTimeout: {}, dataTimeout: {}]: {}", new Object[] { Integer.valueOf(this.ftpClient.getConnectTimeout()), Integer.valueOf(getSoTimeout()), Integer.valueOf(this.dataTimeout), this.ftpClient });
        }
        if (this.ftpClientConfig == null) {
            this.ftpClientConfig = new FTPClientConfigX();
        }
        this.tFtpOperations = new TFtpOperations(this.ftpClient, getFtpClientConfig());
        return this.tFtpOperations;
    }

    public boolean connection(RemoteFileConfiguration configuration)
            throws GenericFileOperationFailedException
    {
        this.log.trace("Connecting using FTPClient: {}", this.ftpClient);

        String host = configuration.getHost();
        int port = configuration.getPort();
        String username = configuration.getUsername();

        if (this.ftpClientConfig != null) {
            this.log.trace("Configuring FTPClient with config: {}", this.ftpClientConfig);
            this.ftpClient.configure(this.ftpClientConfig);
        }

        if (this.log.isTraceEnabled()) {
            this.log.trace("Connecting to {} using connection timeout: {}", configuration.remoteServerInformation(), Integer.valueOf(this.ftpClient.getConnectTimeout()));
        }

        boolean connected = false;
        int attempt = 0;

        while (!connected) {
            try {
                if ((this.log.isTraceEnabled()) && (attempt > 0)) {
                    this.log.trace("Reconnect attempt #{} connecting to {}", Integer.valueOf(attempt), configuration.remoteServerInformation());
                }
                this.ftpClient.connect(host, port);

                int reply = this.ftpClient.getReplyCode();

                if (FTPReply.isPositiveCompletion(reply))
                {
                    connected = true;
                }
                else
                    throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), "Server refused connection");
            } catch (Exception e) {
                if (Thread.currentThread().isInterrupted())
                    throw new GenericFileOperationFailedException("Interrupted during connecting", new InterruptedException("Interrupted during connecting"));
                GenericFileOperationFailedException failed;
                if ((e instanceof GenericFileOperationFailedException))
                    failed = (GenericFileOperationFailedException)e;
                else {
                    failed = new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), e.getMessage(), e);
                }

                this.log.trace("Cannot connect due: {}", failed.getMessage());
                attempt++;
                if (attempt > this.ftpEndpoint.getMaximumReconnectAttempts()) {
                    throw failed;
                }
                if (this.ftpEndpoint.getReconnectDelay() > 0L) {
                    try {
                        Thread.sleep(this.ftpEndpoint.getReconnectDelay());
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new GenericFileOperationFailedException("Interrupted during sleeping", ie);
                    }
                }
            }

        }

        if (configuration.isPassiveMode()) {
            this.log.trace("Using passive mode connections");
            this.ftpClient.enterLocalPassiveMode();
        }

        FtpEndpoint ftpEndpoint1 = this.ftpEndpoint;
        if (ftpEndpoint1.getSoTimeout() > 0) {
            this.log.trace("Using SoTimeout=" + ftpEndpoint1.getSoTimeout());
            try {
                this.ftpClient.setSoTimeout(ftpEndpoint1.getSoTimeout());
            } catch (IOException e) {
                throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), e.getMessage(), e);
            }
        }
        try
        {
            boolean login;
            if (username != null) {
                this.log.trace("Attempting to login user: {} using password: {}", username, configuration.getPassword());
                login = this.ftpClient.login(username, configuration.getPassword());
            } else {
                this.log.trace("Attempting to login anonymous");
                login = this.ftpClient.login("anonymous", "");
            }
            this.log.trace("User {} logged in: {}", username != null ? username : "anonymous", Boolean.valueOf(login));
            if (!login) {
                throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString());
            }
            this.ftpClient.setFileType(configuration.isBinary() ? 2 : 0);
        } catch (IOException e) {
            throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), e.getMessage(), e);
        }

        if (this.ftpEndpoint.getConfiguration().getSiteCommand() != null)
        {
            Iterator it = ObjectHelper.createIterator(this.ftpEndpoint.getConfiguration().getSiteCommand(), "\n");
            while (it.hasNext()) {
                Object next = it.next();
                String command = this.ftpEndpoint.getCamelContext().getTypeConverter().convertTo(String.class, next);
                this.log.trace("Site command to send: {}", command);
                if (command != null) {
                    boolean result = sendSiteCommand(command);
                    if (!result) {
                        throw new GenericFileOperationFailedException("Site command: " + command + " returned false");
                    }
                }
            }
        }
        this.userDirectory = getCurrentDir();
        return true;
    }

    public boolean isConnected2() {
        if(this.ftpClient == null) {
            return false;
        }
        return this.ftpClient.isConnected();
    }

    public void disconnection() throws GenericFileOperationFailedException
    {
        try
        {
            this.log.trace("Client logout");
            this.ftpClient.logout();
        } catch (IOException e) {
            throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), e.getMessage(), e);
        } finally {
            try {
                this.log.trace("Client disconnect");
                this.ftpClient.disconnect();
            } catch (IOException e) {
                throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), e.getMessage(), e);
            }
        }
    }

    public List<FTPFile> displayFiles() throws GenericFileOperationFailedException
    {
        this.log.trace("listFiles()");
        try {
            List list = new ArrayList();
            FTPFile[] files = this.ftpClient.listFiles();

            if (files != null) {
                list.addAll(Arrays.asList(files));
            }
            return list;
        } catch (IOException e) {
            throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), e.getMessage(), e);
        }
    }

    public boolean removeFile(String name) throws GenericFileOperationFailedException {
        this.log.debug("Deleting file: {}", name);

        String target = name;
        String currentDir = null;
        boolean result;
        try { if (this.ftpEndpoint.getConfiguration().isStepwise())
        {
            currentDir = getCurrentDirectory();
            target = FileUtil.stripPath(name);
            try
            {
                jumpCurrentDirectory(FileUtil.onlyPath(name));
            }
            catch (GenericFileOperationFailedException e) {
                jumpCurrentDirectory(currentDir);
                throw e;
            }

        }

            this.log.trace("Client deleteFile: {}", target);
            result = this.ftpClient.deleteFile(target);

            if (currentDir != null)
                jumpCurrentDirectory(currentDir);
        } catch (IOException e) {
            this.log.debug("Deleted file: {}", name);
            result = false;
//            throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), e.getMessage(), e);
        }
        return result;
    }

    public void jumpCurrentDirectory(String path) throws GenericFileOperationFailedException
    {
        this.log.trace("changeCurrentDirectory({})", path);
        if (ObjectHelper.isEmpty(path)) {
            return;
        }

        path = FtpUtils.compactPath(path);

        if (!this.ftpEndpoint.getConfiguration().isStepwise()) {
            doChangeDirectory(path);
            return;
        }

        if (FileUtil.hasLeadingSeparator(path))
        {
            doChangeDirectory(path.substring(0, 1));
            path = path.substring(1);
        }

        String[] dirs = path.split("/|\\\\");

        if ((dirs == null) || (dirs.length == 0))
        {
            doChangeDirectory(path);
            return;
        }

        for (String dir : dirs)
            doChangeDirectory(dir);
    }

    private void doChangeDirectory(String path)
    {
        if ((path == null) || (".".equals(path)) || (ObjectHelper.isEmpty(path))) {
            return;
        }

        this.log.trace("Changing directory: {}", path);
        boolean success;
        try {
            if ("..".equals(path)) {
                changeToParentDirectory();
                success = true;
            } else {
                success = this.ftpClient.changeWorkingDirectory(path);
            }
        } catch (IOException e) {
            throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), e.getMessage(), e);
        }
        if (!success)
            throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), "Cannot change directory to: " + path);
    }

    public boolean retrieveFile(String name) throws GenericFileOperationFailedException {
        File local = new File(FileUtil.normalizePath(this.ftpEndpoint.getLocalWorkDirectory()));
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
            if (this.ftpEndpoint.getConfiguration().isStepwise()) {
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
            result1 = this.ftpClient.retrieveFile(deleted1, os);
            if ((result1) &&
                    (this.ftpEndpoint.isDelete())) {
                try {
                    deleteFile(deleted1);
                }
                catch (GenericFileOperationFailedException gofe)
                {
                }
            }
            if (this.ftpEndpoint.getConfiguration().isStepwise())
                changeCurrentDirectory(File.separator + currentDir);
        }
        catch (IOException var17) {
            this.log.trace("Error occurred during retrieving file: {} to local directory. Deleting local work file: {}", name, temp);
            IOHelper.close(os, "retrieve: " + name, this.log);
            boolean deleted = FileUtil.deleteFile(temp);
            FileUtil.deleteFile(lock);
            if (!deleted) {
                this.log.warn("Error occurred during retrieving file: " + name + " to local directory. Cannot delete local work file: " + temp);
            }

            throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), var17.getMessage(), var17);
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
            if (this.ftpEndpoint.getConfiguration().isStepwise()) {
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
            result1 = this.ftpClient.retrieveFile(deleted1, os);
            if ((result1) &&
                    (this.ftpEndpoint.isDelete())) {
                try {
                    deleteFile(deleted1);
                }
                catch (GenericFileOperationFailedException gofe)
                {
                }
            }
            if (this.ftpEndpoint.getConfiguration().isStepwise())
                jumpCurrentDirectory(File.separator + currentDir);
        }
        catch (IOException var17) {
            this.log.trace("Error occurred during retrieving file: {} to local directory. Deleting local work file: {}", name, temp);
            IOHelper.close(os, "retrieve: " + name, this.log);
            boolean deleted = FileUtil.deleteFile(temp);
            FileUtil.deleteFile(lock);
            if (!deleted) {
                this.log.warn("Error occurred during retrieving file: " + name + " to local directory. Cannot delete local work file: " + temp);
            }

            throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), var17.getMessage(), var17);
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

    public boolean storeFile(File _f_) throws GenericFileOperationFailedException {
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
            IOHelper.close(is, "store: " + _f_.getName(), this.log);
            return false;
        }
        catch (IOException e) {
            IOHelper.close(is, "store: " + _f_.getName(), this.log);
            return false;
        }
        String name = this.ftpEndpoint.getConfiguration().normalizePath(_f_.getName());
        this.log.trace("storeFile({})", name);
        boolean answer = false;
        String currentDir = null;
        String path = FileUtil.onlyPath(name);
        String targetName = name;
        try
        {
            if ((path != null) && (this.ftpEndpoint.getConfiguration().isStepwise())) {
                currentDir = getCurrentDir();
                changeCurrentDirectory(path);
                targetName = FileUtil.stripPath(name);
            }
            answer = doStoreFile(name, targetName, is);
        } finally {
            IOHelper.close(is, "store: " + _f_.getName(), this.log);
            if (currentDir != null) {
                changeCurrentDirectory(currentDir);
            }
            if ((answer) &&
                    (this.ftpEndpoint.isDelete())) {
                _f_.delete();
            }

        }

        return answer;
    }

    private boolean doStoreFile(String name, String targetName, InputStream is1)
            throws GenericFileOperationFailedException
    {
        this.log.trace("doStoreFile({})", targetName);
        if ((this.ftpEndpoint.getFileExist() == GenericFileExist.Ignore) || (this.ftpEndpoint.getFileExist() == GenericFileExist.Fail) || (this.ftpEndpoint.getFileExist() == GenericFileExist.Move)) {
            boolean is = existsFile(targetName);
            if ((is) && (this.ftpEndpoint.getFileExist() == GenericFileExist.Ignore)) {
                this.log.trace("An existing file already exists: {}. Ignore and do not override it.", name);
                return true;
            }

            if ((is) && (this.ftpEndpoint.getFileExist() == GenericFileExist.Fail)) {
                throw new GenericFileOperationFailedException("File already exist: " + name + ". Cannot write new file.");
            }

            if ((is) && (this.ftpEndpoint.getFileExist() == GenericFileExist.Move)) {
                doMoveExistingFile(name, targetName);
            }
        }
        boolean var7;
        try
        {
            StopWatch e1 = new StopWatch();
            this.log.debug("About to store file: {} using stream: {}", targetName, is1);
            boolean answer;
            if (this.ftpEndpoint.getFileExist() == GenericFileExist.Append) {
                this.log.trace("Client appendFile: {}", targetName);
                answer = this.ftpClient.appendFile(targetName, is1);
            } else {
                this.log.trace("Client storeFile: {}", targetName);
                answer = this.ftpClient.storeFile(targetName, is1);
            }

            e1.stop();
            if (this.log.isDebugEnabled()) {
                this.log.debug("Took {} ({} millis) to store file: {} and FTP client returned: {}", new Object[] { TimeUtils.printDuration(e1.taken()), Long.valueOf(e1.taken()), targetName, Boolean.valueOf(answer) });
            }
            var7 = answer;
        } catch (IOException var12) {
            throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), var12.getMessage(), var12);
        } finally {
            IOHelper.close(is1, "store: " + name, this.log);
        }

        return var7;
    }

    private void doMoveExistingFile(String name, String targetName) throws GenericFileOperationFailedException {
        Exchange dummy = this.ftpEndpoint.createExchange();
        Object parent = null;
        String onlyName = FileUtil.stripPath(targetName);
        dummy.getIn().setHeader("CamelFileName", targetName);
        dummy.getIn().setHeader("CamelFileNameOnly", onlyName);
        dummy.getIn().setHeader("CamelFileParent", parent);
        String to = (String)this.ftpEndpoint.getMoveExisting().evaluate(dummy, String.class);
        to = FileUtil.stripLeadingSeparator(to);
        to = this.ftpEndpoint.getConfiguration().normalizePath(to);
        if (ObjectHelper.isEmpty(to)) {
            throw new GenericFileOperationFailedException("moveExisting evaluated as empty String, cannot move existing file: " + name);
        }
        String dir = FileUtil.onlyPath(to);
        if (dir != null) {
            buildDirectory(dir, false);
        }

        if (existsFile(to)) {
            if (!this.ftpEndpoint.isEagerDeleteTargetFile()) {
                throw new GenericFileOperationFailedException("Cannot moved existing file from: " + name + " to: " + to + " as there already exists a file: " + to);
            }

            this.log.trace("Deleting existing file: {}", to);
            try
            {
                boolean result = this.ftpClient.deleteFile(to);
                if (!result)
                    throw new GenericFileOperationFailedException("Cannot delete file: " + to);
            }
            catch (IOException var10) {
                throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), "Cannot delete file: " + to, var10);
            }
        }

        this.log.trace("Moving existing file: {} to: {}", name, to);
        if (!renameFile(targetName, to))
            throw new GenericFileOperationFailedException("Cannot rename file from: " + name + " to: " + to);
    }

    protected FTPClientX createFtpClient()
    {
        return new FTPClientX();
    }

    public FtpConfiguration getConfiguration() {
        if (this.configuration == null) {
            this.configuration = new FtpConfiguration();
        }
        return (FtpConfiguration)this.configuration;
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
        return this.ftpClient.listFiles((FTPCallback)callback);
    }

    public Object getClient()
    {
        return this.ftpClient;
    }

    public String getCurrentDirectory() throws GenericFileOperationFailedException {
        this.log.trace("getCurrentDirectory()");
        try {
            String answer = this.ftpClient.printWorkingDirectory();
            this.log.trace("Current dir: {}", answer);
            return answer;
        } catch (IOException e) {
            throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), e.getMessage(), e);
        }
    }

    public FTPClientX getFtpClientX() {
        return this.ftpClient;
    }

    public void setFtpClient(FTPClientX ftpClient) {
        this.ftpClient = ftpClient;
    }

    public FTPClientConfigX getFtpClientConfig() {
        return this.ftpClientConfig;
    }

    public void setFtpClientConfig(FTPClientConfigX ftpClientConfig) {
        this.ftpClientConfig = ftpClientConfig;
    }

    void setFtpClientParameters(Map<String, Object> ftpClientParameters) {
        this.ftpClientParameters = ftpClientParameters;
    }

    void setFtpClientConfigParameters(Map<String, Object> ftpClientConfigParameters) {
        this.ftpClientConfigParameters = new HashMap(ftpClientConfigParameters);
    }

    public int getSoTimeout() {
        return this.soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public CamelContext getCamelContext() {
        return this.camelContext;
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public String getUserDirectory()
    {
        return this.userDirectory;
    }

    public void setEndpointn(Endpoint endpoint)
    {
        this.ftpEndpoint = ((FtpEndpoint)endpoint);
        super.setEndpoint(this.ftpEndpoint);
    }

    public Endpoint getEndpointn()
    {
        return this.ftpEndpoint;
    }

    public String getCurrentDir()
    {
        return this.ftpEndpoint.getConfiguration().getDirectory();
    }

    public RemoteFileConfiguration getRemoteFileConfiguration()
    {
        return this.ftpEndpoint.getConfiguration();
    }

    public void setUserDirectory(String userDirectory)
    {
        this.userDirectory = userDirectory;
    }

    protected void setProperties(Object bean, Map<String, Object> parameters)
            throws Exception
    {
        EndpointHelper.setReferenceProperties(getCamelContext(), bean, parameters);
        EndpointHelper.setProperties(getCamelContext(), bean, parameters);
    }

    public Object getOperations()
    {
        return this.tFtpOperations;
    }
    public boolean deleteFile(String name) throws GenericFileOperationFailedException { this.log.debug("Deleting file: {}", name);

        String target = name;
        String currentDir = null;
        boolean result;
        try {
            if (this.ftpEndpoint.getConfiguration().isStepwise())
            {
                currentDir = getCurrentDirectory();
                target = FileUtil.stripPath(name);
                try
                {
                    jumpCurrentDirectory(FileUtil.onlyPath(name));
                }
                catch (GenericFileOperationFailedException e) {
                    jumpCurrentDirectory(currentDir);
                    throw e;
                }

            }

            this.log.trace("Client deleteFile: {}", target);
            result = this.ftpClient.deleteFile(target);

            if (currentDir != null)
                jumpCurrentDirectory(currentDir);
        }
        catch (IOException e)
        {
            throw new GenericFileOperationFailedException(this.ftpClient.getReplyCode(), this.ftpClient.getReplyString(), e.getMessage(), e);
        }

        return result;
    }
}
