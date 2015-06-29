package org.humbird.soa.component.esign.tools;

import com.jcraft.jsch.*;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFileConfiguration;
import org.apache.camel.component.file.GenericFileExist;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.component.file.remote.*;
import org.apache.camel.util.*;
import org.humbird.soa.common.net.ftp.FTPCallback;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.KeyPair;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by david on 15/4/7.
 */
public class TSFtpOperations extends SftpOperations
        implements IFileOperations {
    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TSFtpOperations.class);

    private static final Pattern UP_DIR_PATTERN = Pattern.compile("/[^/]+");
    protected GenericFileConfiguration configuration;
    private CamelContext camelContext;
    private Proxy proxy;
    private ChannelSftp channel;
    private Session session;
    private String userDirectory;
    private SftpEndpoint endpoint;
    private TSFtpOperations tsFtpOperations;

    public TSFtpOperations() {
    }

    public TSFtpOperations(Proxy proxy) {
        super(null);
        this.proxy = proxy;
    }

    public Session createSessionX(final RemoteFileConfiguration configuration) throws JSchException {
        JSch jsch = new JSch();
        JSch.setLogger(new JSchLogger());

        SftpConfiguration sftpConfig = (SftpConfiguration) configuration;

        if (ObjectHelper.isNotEmpty(sftpConfig.getCiphers())) {
            LOG.debug("Using ciphers: {}", sftpConfig.getCiphers());
            Hashtable ciphers = new Hashtable();
            ciphers.put("cipher.s2c", sftpConfig.getCiphers());
            ciphers.put("cipher.c2s", sftpConfig.getCiphers());
            JSch.setConfig(ciphers);
        }

        if (ObjectHelper.isNotEmpty(sftpConfig.getPrivateKeyFile())) {
            LOG.debug("Using private keyfile: {}", sftpConfig.getPrivateKeyFile());
            if (ObjectHelper.isNotEmpty(sftpConfig.getPrivateKeyPassphrase()))
                jsch.addIdentity(sftpConfig.getPrivateKeyFile(), sftpConfig.getPrivateKeyPassphrase());
            else {
                jsch.addIdentity(sftpConfig.getPrivateKeyFile());
            }
        }

        if (sftpConfig.getPrivateKey() != null) {
            LOG.debug("Using private key information from byte array");
            byte[] passphrase = null;
            if (ObjectHelper.isNotEmpty(sftpConfig.getPrivateKeyPassphrase())) {
                try {
                    passphrase = sftpConfig.getPrivateKeyPassphrase().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new JSchException("Cannot transform passphrase to byte[]", e);
                }
            }
            jsch.addIdentity("ID", sftpConfig.getPrivateKey(), null, passphrase);
        }

        if (sftpConfig.getPrivateKeyUri() != null) {
            LOG.debug("Using private key uri : {}", sftpConfig.getPrivateKeyUri());
            byte[] passphrase = null;
            if (ObjectHelper.isNotEmpty(sftpConfig.getPrivateKeyPassphrase()))
                try {
                    passphrase = sftpConfig.getPrivateKeyPassphrase().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new JSchException("Cannot transform passphrase to byte[]", e);
                }
            try {
                InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(this.endpoint.getCamelContext().getClassResolver(), sftpConfig.getPrivateKeyUri());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOHelper.copyAndCloseInput(is, bos);
                jsch.addIdentity("ID", bos.toByteArray(), null, passphrase);
            } catch (IOException e) {
                throw new JSchException("Cannot read resource: " + sftpConfig.getPrivateKeyUri(), e);
            }
        }

        if (sftpConfig.getKeyPair() != null) {
            LOG.debug("Using private key information from key pair");
            KeyPair keyPair = sftpConfig.getKeyPair();
            if ((keyPair.getPrivate() != null) && (keyPair.getPublic() != null)) {
                if (((keyPair.getPrivate() instanceof RSAPrivateKey)) && ((keyPair.getPublic() instanceof RSAPublicKey)))
                    jsch.addIdentity(new RSAKeyPairIdentity("ID", keyPair), null);
                else if (((keyPair.getPrivate() instanceof DSAPrivateKey)) && ((keyPair.getPublic() instanceof DSAPublicKey)))
                    jsch.addIdentity(new DSAKeyPairIdentity("ID", keyPair), null);
                else
                    LOG.warn("Only RSA and DSA key pairs are supported");
            } else {
                LOG.warn("PrivateKey and PublicKey in the KeyPair must be filled");
            }
        }

        if (ObjectHelper.isNotEmpty(sftpConfig.getKnownHostsFile())) {
            LOG.debug("Using knownhosts file: {}", sftpConfig.getKnownHostsFile());
            jsch.setKnownHosts(sftpConfig.getKnownHostsFile());
        }

        if (ObjectHelper.isNotEmpty(sftpConfig.getKnownHostsUri())) {
            LOG.debug("Using knownhosts uri: {}", sftpConfig.getKnownHostsUri());
            try {
                InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(this.endpoint.getCamelContext().getClassResolver(), sftpConfig.getKnownHostsUri());
                jsch.setKnownHosts(is);
            } catch (IOException e) {
                throw new JSchException("Cannot read resource: " + sftpConfig.getKnownHostsUri(), e);
            }
        }

        if (sftpConfig.getKnownHosts() != null) {
            LOG.debug("Using knownhosts information from byte array");
            jsch.setKnownHosts(new ByteArrayInputStream(sftpConfig.getKnownHosts()));
        }

        Session session = jsch.getSession(configuration.getUsername(), configuration.getHost(), configuration.getPort());

        if (ObjectHelper.isNotEmpty(sftpConfig.getStrictHostKeyChecking())) {
            LOG.debug("Using StrickHostKeyChecking: {}", sftpConfig.getStrictHostKeyChecking());
            session.setConfig("StrictHostKeyChecking", sftpConfig.getStrictHostKeyChecking());
        }

        session.setServerAliveInterval(sftpConfig.getServerAliveInterval());
        session.setServerAliveCountMax(sftpConfig.getServerAliveCountMax());

        if (sftpConfig.getCompression() > 0) {
            LOG.debug("Using compression: {}", Integer.valueOf(sftpConfig.getCompression()));
            session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
            session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
            session.setConfig("compression_level", Integer.toString(sftpConfig.getCompression()));
        }

        if (sftpConfig.getPreferredAuthentications() != null) {
            LOG.debug("Using PreferredAuthentications: {}", sftpConfig.getPreferredAuthentications());
            session.setConfig("PreferredAuthentications", sftpConfig.getPreferredAuthentications());
        }

        session.setUserInfo(new ExtendedUserInfo() {
            public String getPassphrase() {
                return null;
            }

            public String getPassword() {
                return configuration.getPassword();
            }

            public boolean promptPassword(String s) {
                return true;
            }

            public boolean promptPassphrase(String s) {
                return true;
            }

            public boolean promptYesNo(String s) {
                TSFtpOperations.LOG.warn("Server asks for confirmation (yes|no): " + s + ". Camel will answer no.");

                return false;
            }

            public void showMessage(String s) {
                TSFtpOperations.LOG.trace("Message received from Server: " + s);
            }

            public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
                if (configuration.getPassword() == null) {
                    return new String[0];
                }
                return new String[]{configuration.getPassword()};
            }
        });
        if (this.proxy != null) {
            session.setProxy(this.proxy);
        }

        return session;
    }

    public boolean retrieveFile(String name) throws GenericFileOperationFailedException {
        File local = new File(this.endpoint.getLocalWorkDirectory());

        String currentDir = name;
        File temp;
        File lock;
        FileOutputStream os;
        try {
            temp = new File(local, currentDir + ".inprogress");
            lock = new File(local, currentDir + ".lock");
            local = new File(local, currentDir);
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
        } catch (Exception var18) {
            throw new GenericFileOperationFailedException("Cannot create new local work file: " + local);
        }

        currentDir = null;
        try {
            String e = name;
            if (this.endpoint.getConfiguration().isStepwise()) {
                currentDir = getCurrentDir();
                String deleted1 = FileUtil.onlyPath(name);
                if (deleted1 != null) {
                    changeCurrentDirectory(deleted1);
                }

                e = FileUtil.stripPath(name);
            }
            try {
                FileUtil.createNewFile(lock);
            } catch (IOException e1) {
                return false;
            }
            this.channel.get(e, os);
            if (this.endpoint.isDelete())
                try {
                    removeFile(e);
                } catch (GenericFileOperationFailedException gofe) {
                }
        } catch (SftpException var16) {
            LOG.trace("Error occurred during retrieving file: {} to local directory. Deleting local work file: {}", name, temp);
            IOHelper.close(os, "retrieve: " + name, LOG);
            boolean deleted = FileUtil.deleteFile(temp);
            FileUtil.deleteFile(lock);
            if (!deleted) {
                LOG.warn("Error occurred during retrieving file: " + name + " to local directory. Cannot delete local work file: " + temp);
            }

            throw new GenericFileOperationFailedException("Cannot retrieve file: " + name, var16);
        } finally {
            IOHelper.close(os, "retrieve: " + name, LOG);
            if (currentDir != null) {
                changeCurrentDirectory(File.separator + currentDir);
            }

        }

        LOG.debug(Thread.currentThread().getName() + " <<<>>>" + "Retrieve file to local work file result: true");
        LOG.trace("Renaming local in progress file from: {} to: {}", temp, local);
        try {
            if (!FileUtil.renameFile(temp, local, false)) {
                FileUtil.deleteFile(lock);
                LOG.error("Cannot rename local work file from: " + temp + " to: " + local);
                return false;
            }
            return true;
        } catch (IOException var15) {
            throw new GenericFileOperationFailedException("Cannot rename local work file from: " + temp + " to: " + local, var15);
        }
    }

    public boolean retrieveFile(String name, File local) throws GenericFileOperationFailedException {
        String currentDir = name;
        File temp;
        File lock;
        FileOutputStream os;
        try {
            temp = new File(local, currentDir + ".inprogress");
            lock = new File(local, currentDir + ".lock");
            local = new File(local, currentDir);
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
        } catch (Exception var18) {
            throw new GenericFileOperationFailedException("Cannot create new local work file: " + local);
        }

        try {
            String e = name;
            if (this.endpoint.getConfiguration().isStepwise()) {
                String deleted1 = FileUtil.onlyPath(name);
                if (deleted1 != null) {
                    changeCurrentDirectory(deleted1);
                }

                e = FileUtil.stripPath(name);
            }
            try {
                FileUtil.createNewFile(lock);
            } catch (IOException e1) {
                return false;
            }
            this.channel.get(e, os);
            if (this.endpoint.isDelete())
                try {
                    removeFile(e);
                } catch (GenericFileOperationFailedException gofe) {
                }
        } catch (SftpException var16) {
            LOG.trace("Error occurred during retrieving file: {} to local directory. Deleting local work file: {}", name, temp);
            IOHelper.close(os, "retrieve: " + name, LOG);
            boolean deleted = FileUtil.deleteFile(temp);
            FileUtil.deleteFile(lock);
            if (!deleted) {
                LOG.warn("Error occurred during retrieving file: " + name + " to local directory. Cannot delete local work file: " + temp);
            }

            throw new GenericFileOperationFailedException("Cannot retrieve file: " + name, var16);
        } finally {
            IOHelper.close(os, "retrieve: " + name, LOG);
        }

        LOG.debug(Thread.currentThread().getName() + " <<<>>>" + "Retrieve file to local work file result: true");
        LOG.trace("Renaming local in progress file from: {} to: {}", temp, local);
        try {
            if (!FileUtil.renameFile(temp, local, false)) {
                FileUtil.deleteFile(lock);
                return false;
            }

            return true;
        } catch (IOException var15) {
            throw new GenericFileOperationFailedException("Cannot rename local work file from: " + temp + " to: " + local, var15);
        }
    }

    public boolean storeFile(File _f_) throws GenericFileOperationFailedException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(_f_);
            int size = is.available();
            if (size == 0) {
                if (_f_.delete()) {
                    IOHelper.close(is, "store: " + _f_.getName(), LOG);
                    return false;
                }
                throw new GenericFileOperationFailedException("Delete file: " + _f_.getName() + " failed.");
            }
        } catch (FileNotFoundException e) {
            IOHelper.close(is, "store: " + _f_.getName(), LOG);
            return false;
        } catch (IOException e) {
            IOHelper.close(is, "store: " + _f_.getName(), LOG);
            return false;
        }
        String name = this.endpoint.getConfiguration().normalizePath(_f_.getName());
        LOG.trace("storeFile({})", name);
        boolean answer = false;
        String currentDir = null;
        String path = FileUtil.onlyPath(name);
        String targetName = name;
        try {
            if ((path != null) && (this.endpoint.getConfiguration().isStepwise())) {
                currentDir = getCurrentDir();
                changeCurrentDirectory(path);
                targetName = FileUtil.stripPath(name);
            }

            answer = doStoreFile(name, targetName, is);
        } finally {
            IOHelper.close(is, "store: " + _f_.getName(), LOG);
            if (currentDir != null) {
                changeCurrentDirectory(currentDir);
            }
            if ((answer) &&
                    (this.endpoint.isDelete())) {
                if (_f_.delete()) {
                    return false;
                }
                throw new GenericFileOperationFailedException("Delete file: " + _f_.getName() + " failed.");
            }

        }

        return answer;
    }

    private boolean doStoreFile(String name, String targetName, InputStream is1) throws GenericFileOperationFailedException {
        LOG.trace("doStoreFile({})", targetName);
        if ((this.endpoint.getFileExist() == GenericFileExist.Ignore) || (this.endpoint.getFileExist() == GenericFileExist.Fail) || (this.endpoint.getFileExist() == GenericFileExist.Move)) {
            boolean is = existsFile(targetName);
            if ((is) && (this.endpoint.getFileExist() == GenericFileExist.Ignore)) {
                LOG.trace("An existing file already exists: {}. Ignore and do not override it.", name);
                return true;
            }

            if ((is) && (this.endpoint.getFileExist() == GenericFileExist.Fail)) {
                throw new GenericFileOperationFailedException("File already exist: " + name + ". Cannot write new file.");
            }

            if ((is) && (this.endpoint.getFileExist() == GenericFileExist.Move)) {
                doMoveExistingFile(name, targetName);
            }
        }
        boolean permissions1;
        try {
            StopWatch e = new StopWatch();
            LOG.debug("About to store file: {} using stream: {}", targetName, is1);
            if (this.endpoint.getFileExist() == GenericFileExist.Append) {
                LOG.trace("Client appendFile: {}", targetName);
                this.channel.put(is1, targetName, 2);
            } else {
                LOG.trace("Client storeFile: {}", targetName);
                this.channel.put(is1, targetName);
            }

            e.stop();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Took {} ({} millis) to store file: {} and FTP client returned: true", new Object[]{TimeUtils.printDuration(e.taken()), Long.valueOf(e.taken()), targetName});
            }

            String mode = this.endpoint.getConfiguration().getChmod();
            if (ObjectHelper.isNotEmpty(mode)) {
                int permissions = Integer.parseInt(mode, 8);
                LOG.trace("Setting chmod: {} on file: ", mode, targetName);
                this.channel.chmod(permissions, targetName);
            }

            permissions1 = true;
        } catch (SftpException var12) {
            var12.printStackTrace();
            throw new GenericFileOperationFailedException("Cannot store file: " + name, var12);
        } finally {
            IOHelper.close(is1, "store: " + name, LOG);
        }

        return permissions1;
    }

    private void doMoveExistingFile(String name, String targetName) throws GenericFileOperationFailedException {
        Exchange dummy = this.endpoint.createExchange();
        Object parent = null;
        String onlyName = FileUtil.stripPath(targetName);
        dummy.getIn().setHeader("CamelFileName", targetName);
        dummy.getIn().setHeader("CamelFileNameOnly", onlyName);
        dummy.getIn().setHeader("CamelFileParent", parent);
        String to = (String) this.endpoint.getMoveExisting().evaluate(dummy, String.class);
        to = FileUtil.stripLeadingSeparator(to);
        to = this.endpoint.getConfiguration().normalizePath(to);
        if (ObjectHelper.isEmpty(to)) {
            throw new GenericFileOperationFailedException("moveExisting evaluated as empty String, cannot move existing file: " + name);
        }
        String dir = FileUtil.onlyPath(to);
        if (dir != null) {
            buildDirectory(dir, false);
        }

        if (existsFile(to)) {
            if (!this.endpoint.isEagerDeleteTargetFile()) {
                throw new GenericFileOperationFailedException("Cannot moved existing file from: " + name + " to: " + to + " as there already exists a file: " + to);
            }

            LOG.trace("Deleting existing file: {}", to);
            deleteFile(to);
        }

        LOG.trace("Moving existing file: {} to: {}", name, to);
        if (!renameFile(targetName, to))
            throw new GenericFileOperationFailedException("Cannot rename file from: " + name + " to: " + to);
    }

    public boolean connection(RemoteFileConfiguration configuration)
            throws GenericFileOperationFailedException {
        if (isConnected()) {
            return true;
        }
        boolean connected = false;
        int attempt = 0;

        while (!connected) {
            try {
                if ((LOG.isTraceEnabled()) && (attempt > 0)) {
                    LOG.trace("Reconnect attempt #{} connecting to + {}", Integer.valueOf(attempt), configuration.remoteServerInformation());
                }

                if ((this.channel == null) || (!this.channel.isConnected())) {
                    if ((this.session == null) || (!this.session.isConnected())) {
                        LOG.trace("Session isn't connected, trying to recreate and connect.");
                        this.session = createSessionX(configuration);
                        if (this.endpoint.getConfiguration().getConnectTimeout() > 0) {
                            LOG.trace("Connecting use connectTimeout: " + this.endpoint.getConfiguration().getConnectTimeout() + " ...");
                            this.session.connect(this.endpoint.getConfiguration().getConnectTimeout());
                        } else {
                            LOG.trace("Connecting ...");
                            this.session.connect();
                        }
                    }

                    LOG.trace("Channel isn't connected, trying to recreate and connect.");
                    this.channel = ((ChannelSftp) this.session.openChannel("sftp"));
                    if (this.endpoint.getConfiguration().getConnectTimeout() > 0) {
                        LOG.trace("Connecting use connectTimeout: " + this.endpoint.getConfiguration().getConnectTimeout() + " ...");
                        this.channel.connect(this.endpoint.getConfiguration().getConnectTimeout());
                    } else {
                        LOG.trace("Connecting ...");
                        this.channel.connect();
                    }

                    LOG.trace("Connected to " + configuration.remoteServerInformation());
                }

                connected = true;
            } catch (Exception var8) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new GenericFileOperationFailedException("Interrupted during connecting", new InterruptedException("Interrupted during connecting"));
                }

                GenericFileOperationFailedException failed = new GenericFileOperationFailedException("Cannot connect to " + configuration.remoteServerInformation(), var8);
                LOG.trace("Cannot connect due: {}", failed.getMessage());
                attempt++;
                if (attempt > this.endpoint.getMaximumReconnectAttempts()) {
                    throw failed;
                }

                if (this.endpoint.getReconnectDelay() > 0L) {
                    try {
                        Thread.sleep(this.endpoint.getReconnectDelay());
                    } catch (InterruptedException var7) {
                        Thread.currentThread().interrupt();
                        throw new GenericFileOperationFailedException("Interrupted during sleeping", var7);
                    }
                }
            }
        }
        this.userDirectory = getCurrentDir();
        return true;
    }

    public void disconnection()
            throws GenericFileOperationFailedException {
        disconnect();
        if ((this.session != null) && (this.session.isConnected())) {
            this.session.disconnect();
        }

        if ((this.channel != null) && (this.channel.isConnected())) {
            this.channel.disconnect();
        }
    }

    public List<ChannelSftp.LsEntry> displayFiles()
            throws GenericFileOperationFailedException {
        return displayFiles(".");
    }

    public List<ChannelSftp.LsEntry> displayFiles(String path) throws GenericFileOperationFailedException {
        LOG.trace("listFiles({})", path);
        if (ObjectHelper.isEmpty(path)) {
            path = ".";
        }
        try {
            ArrayList e = new ArrayList();
            Vector files = this.channel.ls(path);
            if (files != null) {
                Iterator i$ = files.iterator();

                while (i$.hasNext()) {
                    Object file = i$.next();
                    e.add((ChannelSftp.LsEntry) file);
                }
            }

            return e;
        } catch (SftpException var6) {
            throw new GenericFileOperationFailedException("Cannot list directory: " + path, var6);
        }
    }

    public boolean removeFile(String name) throws GenericFileOperationFailedException {
        LOG.debug("Deleting file: {}", name);
        try {
            this.channel.rm(name);
            return true;
        } catch (SftpException var3) {
            LOG.debug("Deleted file: {}", name);
//            throw new GenericFileOperationFailedException("Cannot delete file: " + name, var3);
        }
        return false;
    }

    public void jumpCurrentDirectory(String path) throws GenericFileOperationFailedException {
        changeCurrentDirectory(path);
    }

    public boolean isConnected2() {
        if(this.channel == null) return false;
        return this.channel.isConnected();
    }

    public void changeCurrentDirectory(String path) throws GenericFileOperationFailedException {
        LOG.trace("changeCurrentDirectory({})", path);
        if (!ObjectHelper.isEmpty(path)) {
            String before = path;
            char separatorChar = '/';
            path = FileUtil.compactPath(path, separatorChar);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Compacted path: {} -> {} using separator: {}", new Object[]{before, path, Character.valueOf(separatorChar)});
            }

            if (!this.endpoint.getConfiguration().isStepwise()) {
                doChangeDirectory(path);
            } else {
                if (getCurrentDir().startsWith(path)) {
                    String dirs = getCurrentDir().substring(path.length() - (path.endsWith("/") ? 1 : 0));
                    if (dirs.length() == 0) {
                        return;
                    }

                    path = UP_DIR_PATTERN.matcher(dirs).replaceAll("/..").substring(1);
                }

                if (FileUtil.hasLeadingSeparator(path)) {
                    doChangeDirectory(path.substring(0, 1));
                    path = path.substring(1);
                }

                String[] var9 = path.split("/|\\\\");
                if ((var9 != null) && (var9.length != 0)) {
                    String[] arr$ = var9;
                    int len$ = var9.length;

                    for (int i$ = 0; i$ < len$; i$++) {
                        String dir = arr$[i$];
                        doChangeDirectory(dir);
                    }
                } else {
                    doChangeDirectory(path);
                }
            }
        }
    }

    private void doChangeDirectory(String path) {
        if ((path != null) && (!".".equals(path)) && (!ObjectHelper.isEmpty(path))) {
            LOG.trace("Changing directory: {}", path);
            try {
                this.channel.cd(path);
            } catch (SftpException var3) {
                throw new GenericFileOperationFailedException("Cannot change directory to: " + path, var3);
            }
        }
    }

    public String getUserDirectory() {
        return this.userDirectory;
    }

    public void setUserDirectory(String userDirectory) {
        this.userDirectory = userDirectory;
    }

    public void setEndpointn(Endpoint endpoint) {
        this.endpoint = ((SftpEndpoint) endpoint);
        super.setEndpoint(this.endpoint);
    }

    public Endpoint getEndpointn() {
        return this.endpoint;
    }

    public String getCurrentDir() {
        return this.endpoint.getConfiguration().getDirectory();
    }

    public RemoteFileConfiguration getRemoteFileConfiguration() {
        return this.endpoint.getConfiguration();
    }

    public CamelContext getCamelContext() {
        return this.camelContext;
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public Object getOperations() {
        return this.tsFtpOperations;
    }

    public SftpConfiguration getConfiguration() {
        if (this.configuration == null) {
            this.configuration = new SftpConfiguration();
        }
        return (SftpConfiguration) this.configuration;
    }

    public void setConfiguration(GenericFileConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("SFtpConfiguration expected");
        }
        this.configuration = configuration;
    }

    public void doAny(FTPCallback callback) {
    }

    public int list(Object callback)
            throws Exception {
        this.channel.ls(".", (FTPCallback) callback);
        return 0;
    }

    public Object getClient() {
        return this.channel;
    }

    public ChannelSftp getSFtpClientX() {
        return this.channel;
    }

    private static final class JSchLogger
            implements com.jcraft.jsch.Logger {
        public boolean isEnabled(int level) {
            switch (level) {
                case 4:
                    return TSFtpOperations.LOG.isErrorEnabled();
                case 3:
                    return TSFtpOperations.LOG.isErrorEnabled();
                case 2:
                    return TSFtpOperations.LOG.isWarnEnabled();
                case 1:
                    return TSFtpOperations.LOG.isInfoEnabled();
            }
            return TSFtpOperations.LOG.isDebugEnabled();
        }

        public void log(int level, String message) {
            switch (level) {
                case 4:
                    TSFtpOperations.LOG.error("JSCH -> " + message);
                    break;
                case 3:
                    TSFtpOperations.LOG.error("JSCH -> " + message);
                    break;
                case 2:
                    TSFtpOperations.LOG.warn("JSCH -> " + message);
                    break;
                case 1:
                    TSFtpOperations.LOG.info("JSCH -> " + message);
                    break;
                default:
                    TSFtpOperations.LOG.debug("JSCH -> " + message);
            }
        }
    }

    public static abstract interface ExtendedUserInfo extends UserInfo, UIKeyboardInteractive {
    }
}