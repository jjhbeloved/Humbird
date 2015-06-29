package org.humbird.soa.component.esign.tools;

import com.jcraft.jsch.ChannelSftp;
import org.apache.camel.Endpoint;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.component.file.remote.FtpEndpoint;
import org.apache.camel.component.file.remote.FtpsEndpoint;
import org.apache.camel.component.file.remote.RemoteFileConfiguration;
import org.apache.camel.component.file.remote.SftpEndpoint;
import org.humbird.soa.common.exception.ConnectionException;
import org.humbird.soa.common.exception.FileOperationException;
import org.humbird.soa.common.exception.InitUriException;
import org.humbird.soa.common.io.FileReaderExt;
import org.humbird.soa.common.model.common.FBufferModel;
import org.humbird.soa.common.model.common.MapModel;
import org.humbird.soa.common.model.common.PatternModel;
import org.humbird.soa.common.net.ftp.FTPCallback;
import org.humbird.soa.common.net.ftp.FTPFile;
import org.humbird.soa.common.tools.TFile;
import org.humbird.soa.common.tools.TIO;
import org.humbird.soa.common.tools.TString;
import org.humbird.soa.component.esign.log.LogProxy;
import org.humbird.soa.component.esign.model.*;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by david on 15/4/7.
 */
public class TOperations {
    private static final int COLUMNS = 512;

    public static void getFile(FTPModel model)
            throws FileOperationException, IOException {
        IFileOperations operations = model.getOperations();
        UUID uuid = UUID.randomUUID();

        File uuidFile = new File(new StringBuilder().append(model.getFileModel().getLazDir()).append(File.separator).append(uuid).toString());

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = TIO.create(uuidFile);
        } catch (IOException e) {
            TIO.close(bufferedWriter);
            throw e;
        }
        model.getFileModel().setLazBuffer(bufferedWriter);
        FTPCallback callback = new FTPCallBackDonwnload1Impl(model);
        try {
            operations.list(callback);
            if ((callback.getCounts() > 0) && (callback.getBuf().size() > 0)) {
                TIO.append(bufferedWriter, (StringBuilder) callback.getBuf().get(0));
                callback.getBuf().clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            String err = new StringBuilder().append("O2P find SCAN URI directory or file Failed. ").append(e.getMessage()).toString();
            model.getLOGGER().info(err);
            throw new FileOperationException(err);
        } finally {
            TIO.close(bufferedWriter);
        }
        FileReaderExt readerExt = null;
        try {
            readerExt = new FileReaderExt(uuidFile, "UTF-8", TFile.NEW_LINE, " ");
            for (List vals : readerExt) {
                String name = (String) vals.get(0);
                String rm = (String) vals.get(1);
                if ("true".equals(rm)) {
                    if (operations.removeFile(name)) {
                        String info = LogProxy.assembly("eSign", model.getProject(), "FAILED", model.getGlobal(), uuid, new StringBuilder().append("O2P remove file").append(name).append(". ").toString());
                        model.getLOGGER().trace(info);
                    }
                } else callback.any(name);
            }
        } catch (FileNotFoundException e) {
            throw e;
        } finally {
            if (readerExt != null)
                try {
                    readerExt.close();
                } catch (IOException e) {
                }
            uuidFile.delete();
        }
    }

    public static void mapping(FTPModel model) throws Exception {
        IFileOperations operations = getOperations(model.getEndpoint(), model);
        FTPCallback callback = new FTPCallBackMapping1Impl(model);

        operations.list(callback);
        Map rules = model.getRules();
        Iterator iterator = rules.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            FBufferModel tbuFBufferModel = (FBufferModel) entry.getValue();
            StringBuilder sbuffer = tbuFBufferModel.getStringBuilder();
            BufferedWriter buffer = null;
            try {
                if (sbuffer.length() > 0) {
                    buffer = tbuFBufferModel.getBufferedWriter();
                    TIO.append(buffer, sbuffer.append(TFile.NEW_LINE));
                    sbuffer.setLength(0);
                    tbuFBufferModel.setCount(0);
                }
            } finally {
                TIO.close(buffer);
            }
        }
        List dels = model.getDels();
        int i = 0;
        for (int siz = dels.size(); i < siz; i++) {
            String name = (String) dels.get(i);
//            String del = LogProxy.assembly("eSign", model.getProject(), "FAILED", model.getGlobal(), UUID.randomUUID(), new StringBuilder().append("O2P remove deprecated file: ").append(name).toString());
//            model.getLOGGER().info(del);
            operations.removeFile(name);
        }
        if (dels.size() > 0)
            dels.clear();
    }

    public static IFileOperations switchOperations(FTPModel model) throws InitUriException, ConnectionException, FileOperationException {
        IFileOperations operations = model.getOperations();
        if (!isURIEqual(model.getSrcRemoteFileConfiguration(), model.getTarRemoteFileConfiguration())) {
            try {
                operations.disconnection();
            } catch (GenericFileOperationFailedException e) {
                String err = new StringBuilder().append("O2P disconncet URI Failed. Please manual disconnect. ").append(e.getMessage()).toString();
                throw new ConnectionException(err);
            }
            try {
                operations = getOperations(model);
            } catch (Exception e) {
                String err = new StringBuilder().append("O2P Initial URI Failed. Please check URI. ").append(e.getMessage()).toString();
                throw new InitUriException(err);
            }
            try {
                operations.connection(model.getTarRemoteFileConfiguration());
            } catch (GenericFileOperationFailedException e) {
                String err = new StringBuilder().append("O2P conncet URI Failed. Please check URI File Server is running? ").append(e.getMessage()).toString();
                throw new ConnectionException(err);
            }
        } else {
            operations.setEndpointn(model.getEndpoint());
        }
        try {
            operations.jumpCurrentDirectory(new StringBuilder().append(File.separator).append(model.getTarRemoteFileConfiguration().getDirectory()).toString());
        } catch (GenericFileOperationFailedException e) {
            String err = new StringBuilder().append("O2P change path Failed. Please check URI connection. ").append(File.separator).append(model.getTarRemoteFileConfiguration().getDirectory()).append(" is exsist? ").append(e.getMessage()).toString();
            throw new FileOperationException(err);
        }
        model.setOperations(operations);
        model.setSrcRemoteFileConfiguration(model.getTarRemoteFileConfiguration());
        return operations;
    }

    public static boolean isURIEqual(RemoteFileConfiguration r1, RemoteFileConfiguration r2) {
        String host = r1.getHost();
        int port = r1.getPort();
        String user = r1.getUsername();
        if ((host.equalsIgnoreCase(r2.getHost())) && (port == r2.getPort()) && (user.equalsIgnoreCase(r2.getUsername())))
            return true;
        return false;
    }

    public static IFileOperations getOperations(FTPModel model)
            throws Exception {
        IFileOperations operations = null;
        if (!(model.getEndpoint() instanceof FtpsEndpoint)) {
            if ((model.getEndpoint() instanceof FtpEndpoint)) {
                FtpEndpoint ftpEndpoint = (FtpEndpoint) model.getEndpoint();
                ftpEndpoint.setLocalWorkDirectory(model.getFileModel().getDataDir().getAbsolutePath());
                TFtpOperations tFtpOperations = new TFtpOperations();
                tFtpOperations = tFtpOperations.createTFtpOperations();
                tFtpOperations.setEndpointn(ftpEndpoint);
                tFtpOperations.setCamelContext(ftpEndpoint.getCamelContext());
                operations = tFtpOperations;
            } else if ((model.getEndpoint() instanceof SftpEndpoint)) {
                SftpEndpoint sftpEndpoint = (SftpEndpoint) model.getEndpoint();
                sftpEndpoint.setLocalWorkDirectory(model.getFileModel().getDataDir().getAbsolutePath());
                TSFtpOperations tsFtpOperations = new TSFtpOperations();
                tsFtpOperations.setEndpointn(sftpEndpoint);
                tsFtpOperations.setCamelContext(sftpEndpoint.getCamelContext());
                operations = tsFtpOperations;
            }
        }
        model.setOperations(operations);
        return operations;
    }

    public static IFileOperations getOperations(Endpoint endpoint, FTPModel ftpModel) throws ConnectionException, FileOperationException, InitUriException {
        RemoteFileConfiguration fileConfiguration = null;
        if ((endpoint instanceof FtpEndpoint)) {
            FtpEndpoint ftpEndpoint = (FtpEndpoint) endpoint;
            fileConfiguration = ftpEndpoint.getConfiguration();
        } else if ((endpoint instanceof SftpEndpoint)) {
            SftpEndpoint sftpEndpoint = (SftpEndpoint) endpoint;
            fileConfiguration = sftpEndpoint.getConfiguration();
        }

        ftpModel.setTarRemoteFileConfiguration(fileConfiguration);
        ftpModel.setEndpoint(endpoint);
        return switchOperations(ftpModel);
    }

    public static void assemblyFBufferModel(FBufferModel fBufferModel, File file, BufferedWriter bufferedWriter, StringBuilder stringBuilder, int count) {
        fBufferModel.setFile(file);
        fBufferModel.setBufferedWriter(bufferedWriter);
        fBufferModel.setStringBuilder(stringBuilder);
        fBufferModel.setCount(count);
    }

    public static void assemblyFileLinkedModel(FileLinkedModel fileLinkedModel, String name, List<String> subs, File file, String sub) {
        fileLinkedModel.setTrueName(name);
        fileLinkedModel.getSubNames().addAll(subs);
        fileLinkedModel.setFile(file);
        fileLinkedModel.setSub(sub);
    }

    public static void assemblyFile(List<MapModel> mapModels, StringBuilder stringBuilder, int count, String sub) {
        int i = 0;
        for (int size = mapModels.size(); i < size; i++) {
            MapModel mapModel = mapModels.get(i);
            stringBuilder.append(mapModel.getTrue(new OutputCallback(count)));
            if (i < size - 1)
                stringBuilder.append(sub);
        }
    }

    public static void assemblyHead(List<MapModel> list, StringBuilder stringBuilder, String split) throws IOException {
        int i = 0;
        for (int size = list.size(); i < size; i++) {
            MapModel mapModel = list.get(i);
            stringBuilder.append(mapModel.getTrue(null));
            if (i < size - 1) {
                stringBuilder.append(split);
            }
        }
        stringBuilder.append(TFile.NEW_LINE);
    }

    public static void assemblyBody(List<MapModel> list, StringBuilder stringBuilder, List<String> ns, String nxml, String npdf, String split) throws IOException {
        int i = 0;
        for (int size = list.size(); i < size; i++) {
            MapModel mapModel = list.get(i);
            stringBuilder.append(mapModel.any(new CTRCallback(ns, nxml, npdf)));
            if (i < size - 1) {
                stringBuilder.append(split);
            }
        }
        stringBuilder.append(TFile.NEW_LINE);
    }

    public static void assemblyTail(List<MapModel> list, StringBuilder stringBuilder, int count, String split) throws IOException {
        int i = 0;
        for (int size = list.size(); i < size; i++) {
            MapModel mapModel = list.get(i);
            stringBuilder.append(mapModel.getTrue(null));
            if (i < size - 1) {
                stringBuilder.append(split);
            }
        }
        stringBuilder.append(split).append(count);
    }

    static abstract class AbstractFTPCallBack {
        protected final Logger logger;
        protected final Map<String, Map<String, FileLinkedModel>> _tables_;
        protected final String project;
        protected final UUID global;
        protected final FileModel fileModel;
        protected final String suffix;
        protected final int suffix_length;
        protected final IFileOperations iFileOperations;
        protected final double time;
        protected final long beginTime = System.currentTimeMillis();
        protected final PatternModel indexs;
        protected final DocDateModel docDateModel;
        protected final Map<String, FBufferModel> rules;
        protected final List<String> dels;
        protected List<StringBuilder> buf = new ArrayList();

        protected int index = 0;

        protected boolean flag = false;

        public AbstractFTPCallBack(FTPModel model) {
            this.logger = model.getLOGGER();
            this._tables_ = model.get_tables_();
            this.project = model.getProject();
            this.global = model.getGlobal();
            this.fileModel = model.getFileModel();
            this.suffix = model.getSuffix();
            this.suffix_length = this.suffix.length();
            this.time = model.getTime();
            this.iFileOperations = model.getOperations();
            this.indexs = model.getIndexs();
            this.docDateModel = model.getDocDateModel();
            this.rules = model.getRules();
            this.dels = model.getDels();
        }

        protected int listFTP(FTPFile ftpFile) {
            if (ftpFile.isDirectory()) {
                return 0;
            }
            String name = ftpFile.getName();
            if (!name.substring(name.length() - this.suffix_length).equalsIgnoreCase(this.suffix)) {
                return 0;
            }
            long fileTime = ftpFile.getTimestamp().getTimeInMillis();
            try {
                return list(name, (this.beginTime - fileTime) / this.time > 1.0D);
            } catch (IOException e) {
            }
            return 0;
        }

        protected int listSFTP(ChannelSftp.LsEntry lsEntry) {
            if (lsEntry.getAttrs().isDir()) {
                return 0;
            }
            String name = lsEntry.getFilename();
            if (!name.substring(name.length() - this.suffix_length).equalsIgnoreCase(this.suffix)) {
                return 0;
            }
            int fileTime = lsEntry.getAttrs().getMTime();
            double overTime = this.time / 1000.0D;
            long nowTime = this.beginTime / 1000L;
            try {
                return list(name, (nowTime - fileTime) / overTime > 1.0D);
            } catch (IOException e) {
            }
            return 0;
        }

        protected int listX(String name, boolean rm) {
            try {
                if (this.buf.size() > 0)
                    ((StringBuilder) this.buf.get(0)).append(name).append(" ").append(rm).append(TFile.NEW_LINE);
                else {
                    this.buf.add(0, new StringBuilder(name).append(" ").append(rm).append(TFile.NEW_LINE));
                }
                this.index += 1;
                if (this.index == 512) {
                    TIO.append(this.fileModel.getLazBuffer(), (StringBuilder) this.buf.get(0));
                    this.buf.clear();
                    this.index = 0;
                }
            } catch (IOException e) {
                String err = LogProxy.assembly("eSign", this.project, "FAILED", this.global, UUID.randomUUID(), "O2P find SCAN URI directory or file Failed. Please Local System IO. " + e.getMessage());
                this.logger.info(err);
                return 1;
            }
            return 0;
        }

        protected abstract int list(String paramString, boolean paramBoolean)
                throws IOException;
    }

    static class FTPCallBackMapping1Impl extends TOperations.AbstractFTPCallBack
            implements FTPCallback {

        public FTPCallBackMapping1Impl(FTPModel model) {
            super(model);
        }

        protected int list(String name, boolean flag) throws IOException {
            if (flag) {
                this.dels.add(name);
            } else {
                List ns = TString.splitSimpleString(name, this.indexs.getSub());
                String date = (String) ns.get(this.docDateModel.getNxmlDate());
                String fname = new StringBuilder().append(date).append(this.global).toString();
                FBufferModel fBufferModel;
                BufferedWriter bufferedWriter;
                StringBuilder sbuffer;
                Integer counts;
                if ((fBufferModel = this.rules.get(fname)) == null) {
                    fBufferModel = new FBufferModel();
                    File file = new File(this.fileModel.getMappingDir(), new StringBuilder().append(date).append(this.global).toString());
                    bufferedWriter = TIO.create(file);
                    sbuffer = new StringBuilder("");
                    counts = Integer.valueOf(0);
                    TOperations.assemblyFBufferModel(fBufferModel, file, bufferedWriter, sbuffer, counts.intValue());
                    this.rules.put(fname, fBufferModel);
                    sbuffer.append(name);
                } else {
                    bufferedWriter = fBufferModel.getBufferedWriter();
                    sbuffer = fBufferModel.getStringBuilder();
                    counts = Integer.valueOf(fBufferModel.getCount() + 1);
                    fBufferModel.setCount(counts.intValue());
                    if (sbuffer.length() > 0) {
                        sbuffer.append(" ");
                    }
                    sbuffer.append(name);
                }
                if (counts.intValue() == 512) {
                    TIO.append(bufferedWriter, sbuffer.append(TFile.NEW_LINE));
                    sbuffer.setLength(0);
                    fBufferModel.setCount(0);
                }
            }
            return 0;
        }

        public int select(ChannelSftp.LsEntry lsEntry) {
            return listSFTP(lsEntry);
        }

        public int select(FTPFile ftpFile) {
            return listFTP(ftpFile);
        }

        public boolean any(Object o) {
            return true;
        }

        public List<StringBuilder> getBuf() {
            return this.buf;
        }

        public int getCounts() {
            return this.index;
        }

        public boolean isFlag() {
            return this.flag;
        }
    }

    static class FTPCallBackDonwnload1Impl extends TOperations.AbstractFTPCallBack
            implements FTPCallback {

        public FTPCallBackDonwnload1Impl(FTPModel model) {
            super(model);
        }

        public int select(ChannelSftp.LsEntry lsEntry) {
            return listSFTP(lsEntry);
        }

        public int select(FTPFile ftpFile) {
            return listFTP(ftpFile);
        }

        public boolean any(Object object) {
            String name = (String) object;
            List ns = TString.splitSimpleString(name, this.indexs.getSub(), this.indexs.getVals(), null);

            String date = (String) ns.get(this.docDateModel.getNpdfDate());
            File _file_ = new File(this.fileModel.getDataDir(), date);
            if (!_file_.exists()) _file_.mkdirs();
            try {
                if (this.iFileOperations.retrieveFile(name, _file_)) {
                    String newStr = (String) ns.get(ns.size() - 1);
                    FileLinkedModel fileLinkedModel = new FileLinkedModel();
                    TOperations.assemblyFileLinkedModel(fileLinkedModel, name, ns, new File(_file_, name), this.indexs.getSub());
                    Map _hashmap_ = null;
                    if (this._tables_.containsKey(date)) {
                        _hashmap_ = (Map) this._tables_.get(date);
                        _hashmap_.put(newStr, fileLinkedModel);
                    } else {
                        _hashmap_ = new HashMap();
                        _hashmap_.put(newStr, fileLinkedModel);
                        this._tables_.put(date, _hashmap_);
                    }
                }
            } catch (GenericFileOperationFailedException e) {
                String err = LogProxy.assembly("eSign", this.project, "FAILED", this.global, UUID.randomUUID(), "O2P find SCAN URI directory or file Failed. Please check URI connection. " + e.getMessage());
                this.logger.info(err);
                return false;
            }
            return true;
        }

        public List<StringBuilder> getBuf() {
            return this.buf;
        }

        public int getCounts() {
            return this.index;
        }

        public boolean isFlag() {
            return this.flag;
        }

        protected int list(String name, boolean rm) {
            return listX(name, this.flag);
        }
    }
}