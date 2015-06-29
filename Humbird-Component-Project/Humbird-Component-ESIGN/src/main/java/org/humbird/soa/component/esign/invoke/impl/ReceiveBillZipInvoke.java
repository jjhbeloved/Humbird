package org.humbird.soa.component.esign.invoke.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.component.file.remote.RemoteFileConfiguration;
import org.apache.camel.impl.DefaultCamelContext;
import org.humbird.soa.common.utils.FileUtil;
import org.humbird.soa.common.codec.Codec;
import org.humbird.soa.common.exception.ConnectionException;
import org.humbird.soa.common.exception.FileOperationException;
import org.humbird.soa.common.exception.InitUriException;
import org.humbird.soa.common.model.common.FBufferModel;
import org.humbird.soa.common.model.common.PatternModel;
import org.humbird.soa.common.model.common.PropsModel;
import org.humbird.soa.common.tools.PatternLayout;
import org.humbird.soa.common.tools.TIO;
import org.humbird.soa.common.tools.TString;
import org.humbird.soa.common.tools.TTimestamp;
import org.humbird.soa.component.esign.business.EsignAction;
import org.humbird.soa.component.esign.business.impl.ReceiveBillZipAction;
import org.humbird.soa.component.esign.invoke.EsignInvoke;
import org.humbird.soa.component.esign.log.LogProxy;
import org.humbird.soa.component.esign.model.*;
import org.humbird.soa.component.esign.tools.IFileOperations;
import org.humbird.soa.component.esign.tools.TOperations;
import org.humbird.soa.core.HumbirdSession;
import org.humbird.soa.core.model.PropertyModel;
import org.humbird.soa.core.util.HumbirdUtil;
import org.humbird.soa.core.util.KeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by david on 15/4/7.
 */
public class ReceiveBillZipInvoke extends EsignInvoke {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveBillZipInvoke.class);
    private static String _temp_directory_;
    private static String SUFFIX;
    private static int SUFFIX_LENGTH;
    private static String SSUFFIX;
    private static int SSUFFIX_LENGTH;
    private static File _tar_directory_;
    private static File _gz_directory_;
    private static File _put_directory_;
    private static File _failed_directory_;
    private static File _data_directory_;
    private static File _mapping_directory_;
    private static File _bak_directory_;
    private static File _laz_directory_;
    private static TimeModel timeModel = new TimeModel();

    private static CamelContext camelContext = new DefaultCamelContext();

    private static PatternModel indexs = new PatternModel();

    private static PatternModel sendPatternModel = new PatternModel();

    private static CTRModel ctrModel = new CTRModel();

    private static DocDateModel docDateModel = new DocDateModel();

    public void init()
            throws Exception {
        HumbirdSession session = HumbirdUtil.getSession(KeyUtil.CUSTOM_KEY);
        Map maps = (Map) session.getValue();
        Map map = (Map) maps.get("esign");
        PropertyModel propertyModel = (PropertyModel) map.get("esign/receiveBillZip.properties");
        List props = propertyModel.getPropsModel().getProps();
        int encrpyt = propertyModel.getEncrpyt();
        int i = 0;
        ctrModel.clear();
        indexs.clear();
        sendPatternModel.clear();
        for (int siz = props.size(); i < siz; i++) {
            PropsModel.Prop prop = (PropsModel.Prop) props.get(i);
            if (prop != null) {
                LOGGER.debug(new StringBuilder().append("Prop: ").append(prop.getKey()).append(" - - - ").append(prop.getVal()).toString());
                names.put(prop.getKey(), Codec.autoDec(prop, encrpyt));
            }
        }

        if (names.containsKey(DELETE_MAPPING_DATE)) {
            timeModel.setDeprecatedDelete(Integer.parseInt(names.get(DELETE_MAPPING_DATE)));
        }
        if (names.containsKey(DELETE_MAPPING_TYPE)) {
            timeModel.setDeprecatedDeleteType(Byte.parseByte(names.get(DELETE_MAPPING_TYPE)));
        }

        if (!names.containsKey(OUTPUT_PATTERN)) {
            throw new Exception("error, please input output file pattern.");
        }
        String outputPattern = names.get(OUTPUT_PATTERN);

        if (names.containsKey(OUTPUT)) {
            String clazz = names.get(OUTPUT);
            PatternLayout patternLayout = null;
            try {
                patternLayout = (PatternLayout) Class.forName(clazz).newInstance();
                sendPatternModel.setClazzName(clazz);
                sendPatternModel.setPatternLayout(patternLayout);
            } catch (InstantiationException e) {
                throw e;
            } catch (IllegalAccessException e) {
                throw e;
            } catch (ClassNotFoundException e) {
                try {
                    patternLayout = (PatternLayout) Class.forName("org.humbird.soa.common.tools.TPatternLayout").newInstance();
                } catch (IllegalAccessException e1) {
                    throw e1;
                } catch (ClassNotFoundException e1) {
                    throw e1;
                } catch (InstantiationException e1) {
                    throw e1;
                }
            }

            List list = patternLayout.conversionPattern(outputPattern);
            sendPatternModel.getVals().addAll(list);
            if (names.containsKey(OUTPUT_TYPE)) {
                sendPatternModel.setSub(names.get(OUTPUT_TYPE));
            }
            if (names.containsKey(MATCH)) {
                indexs.getVals().addAll(indexs.getPatternLayout().conversionPattern(names.get(MATCH)));
            }
            if (names.containsKey(MATCH_TYPE)) {
                indexs.setSub(names.get(MATCH_TYPE));
            }

            if (names.containsKey(NXML_DATE)) {
                docDateModel.setNxmlDate(Integer.parseInt(names.get(NXML_DATE)));
            }

            if (names.containsKey(NPDF_DATE)) {
                docDateModel.setNpdfDate(Integer.parseInt(names.get(NPDF_DATE)));
            }

            if (names.containsKey(VAILD_SPLIT))
                ctrModel.getVaild().setSplit(ctrModel.getPatternLayout().conversionPattern(names.get(VAILD_SPLIT)));
            else {
                throw new Exception("error, please vaild split parameter.");
            }

            if (names.containsKey(VAILD_HEAD)) {
                ctrModel.getVaild().setHead(ctrModel.getPatternLayout().conversionPattern(names.get(VAILD_HEAD)));
            }

            if (names.containsKey(VAILD_BODY)) {
                ctrModel.getVaild().setBody(ctrModel.getPatternLayout().conversionPattern(names.get(VAILD_BODY)));
            }

            if (names.containsKey(VAILD_TAIL)) {
                ctrModel.getVaild().setTail(ctrModel.getPatternLayout().conversionPattern(names.get(VAILD_TAIL)));
            }

            if (names.containsKey(INVAILD_SPLIT))
                ctrModel.getInvaild().setSplit(ctrModel.getPatternLayout().conversionPattern(names.get(INVAILD_SPLIT)));
            else {
                throw new Exception("error, please vaild split parameter.");
            }

            if (names.containsKey(INVAILD_HEAD)) {
                ctrModel.getInvaild().setHead(ctrModel.getPatternLayout().conversionPattern(names.get(INVAILD_HEAD)));
            }

            if (names.containsKey(INVAILD_BODY)) {
                ctrModel.getInvaild().setBody(ctrModel.getPatternLayout().conversionPattern(names.get(INVAILD_BODY)));
            }

            if (names.containsKey(INVAILD_TAIL)) {
                ctrModel.getInvaild().setTail(ctrModel.getPatternLayout().conversionPattern(names.get(INVAILD_TAIL)));
            }
        }
        SUFFIX = names.get(SCAN_SUFFIX);
        SUFFIX_LENGTH = SUFFIX.length();
        SSUFFIX = names.get(SCANED_SUFFIX);
        SSUFFIX_LENGTH = SSUFFIX.length();
    }

    public void destroy() throws Exception {
    }

    private static void mkdir_init() {
        _temp_directory_ = names.get(TEMPLATE_DIRECTORY);
        _tar_directory_ = new File(_temp_directory_, "_tar");
        _gz_directory_ = new File(_temp_directory_, "_gz");
        _put_directory_ = new File(_temp_directory_, "_put");
        _failed_directory_ = new File(_temp_directory_, "_failed");
        _data_directory_ = new File(_temp_directory_, "_datas");
        _mapping_directory_ = new File(_temp_directory_, "_mapping");
        _bak_directory_ = new File(_temp_directory_, "_bak");
        _laz_directory_ = new File(_temp_directory_, "_laz");
        if (!_put_directory_.exists()) {
            _put_directory_.mkdirs();
        }
        if (!_tar_directory_.exists()) {
            _tar_directory_.mkdirs();
        }
        if (!_gz_directory_.exists()) {
            _gz_directory_.mkdirs();
        }
        if (!_failed_directory_.exists()) {
            _failed_directory_.mkdirs();
        }
        if (!_data_directory_.exists()) {
            _data_directory_.mkdirs();
        }
        if (!_mapping_directory_.exists()) {
            _mapping_directory_.mkdirs();
        }
        if (!_bak_directory_.exists()) {
            _bak_directory_.mkdirs();
        }
        if (!_laz_directory_.exists())
            _laz_directory_.mkdirs();
    }

    public void invoke()
            throws Exception {
        mkdir_init();
        UUID global = UUID.randomUUID();
        Map _tables_ = new HashMap();
        Map _lines_ = new HashMap();
        Map _counts_ = new HashMap();
        Map _read_ = new HashMap();
        List mapping_lock = new ArrayList();
        Map _scans_ = new HashMap();
        long time = Long.parseLong(names.get(DAY)) * 24L * 60L * 60L * 1000L;

        FileModel fileModel = new FileModel();
        fileModel.setLazDir(_laz_directory_);
        fileModel.setDataDir(_data_directory_);
        fileModel.setMappingDir(_mapping_directory_);
        fileModel.setRootDir(_temp_directory_);
        fileModel.setFailedDir(_failed_directory_);
        fileModel.setBakDir(_bak_directory_);
        fileModel.setTarDir(_tar_directory_);
        fileModel.setPutDir(_put_directory_);
        fileModel.setGzDir(_gz_directory_);

        FTPModel ftpModel = new FTPModel(global, names.get(NAME), SUFFIX_LENGTH);
        ftpModel.set_tables_(_tables_);
        ftpModel.setTime(time);
        ftpModel.setLOGGER(LOGGER);
        ftpModel.set_lines_(_lines_);
        ftpModel.set_read_(_read_);
        ftpModel.set_counts_(_counts_);
        ftpModel.setMapping_lock(mapping_lock);
        ftpModel.set_scans_(_scans_);
        ftpModel.setTimeModel(timeModel);
        ftpModel.setFileModel(fileModel);
        ftpModel.setPatternModel(sendPatternModel);
        ftpModel.setIndexs(indexs);
        ftpModel.setDocDateModel(docDateModel);
        ftpModel.setCtrModel(ctrModel);
        try {
            deleteDepractedMapping(ftpModel);

            long start = System.currentTimeMillis();
            download1(camelContext, ftpModel);
            LOGGER.debug(Thread.currentThread().getName() + " <<<>>>" + new StringBuilder().append("download scan time : ").append(System.currentTimeMillis() - start).toString());

            int _size_ = ftpModel.get_tables_().size();
            if (_size_ == 0) {
                LOGGER.info(Thread.currentThread().getName() + " <<<>>>" + LogProxy.assembly("eSign", names.get(NAME), "SUCCESS", global, UUID.randomUUID(), "O2P scan end. ePDF is empty."));
                try {
                    IFileOperations operations = ftpModel.getOperations();
                    operations.disconnection();
                } catch (GenericFileOperationFailedException e) {
                    String err = new StringBuilder().append("O2P disconncet SCAN URI Failed. Please manual disconnect. ").append(e.getMessage()).toString();
                    throw new ConnectionException(err);
                }
            } else {
                start = System.currentTimeMillis();
                mapping(camelContext, ftpModel);
                LOGGER.debug(Thread.currentThread().getName() + " <<<>>>" + new StringBuilder().append("mapping scan time : ").append(System.currentTimeMillis() - start).toString());

                start = System.currentTimeMillis();
                accoss2(ftpModel);
                LOGGER.debug(Thread.currentThread().getName() + " <<<>>>" + new StringBuilder().append("accross scan time : ").append(System.currentTimeMillis() - start).toString());

                start = System.currentTimeMillis();
                push1(camelContext, ftpModel);
                LOGGER.debug(Thread.currentThread().getName() + " <<<>>>" + new StringBuilder().append("push1 scan time : ").append(System.currentTimeMillis() - start).toString());

                start = System.currentTimeMillis();
                push2(camelContext, ftpModel);
                LOGGER.debug(Thread.currentThread().getName() + " <<<>>>" + new StringBuilder().append("push2 scan time : ").append(System.currentTimeMillis() - start).toString());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            IFileOperations operations2 = ftpModel.getOperations();
            if ((operations2 != null) && (operations2.isConnected2())) {
                operations2.disconnection();
            }
            LOGGER.info(Thread.currentThread().getName() + " <<<>>>" + _tables_.size());
            releaseLock(ftpModel, _tables_);
            releaseLock(ftpModel, _scans_);
            ftpModel.clear();
        }
    }

    // release file lock
    private void releaseLock(FTPModel model, Map<String, Map<String, FileLinkedModel>> _tables_) {
        Iterator iterator = _tables_.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            Map datas = (Map) entry.getValue();
            Iterator iter = datas.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry1 = (Map.Entry) iter.next();
                FileLinkedModel value = (FileLinkedModel) entry1.getValue();
                String lock = new StringBuilder().append(value.getFile()).append(".lock").toString();
                LOGGER.debug(Thread.currentThread().getName() + " <<<>>>");
                FileUtil.deleteFile(new File(lock));
            }
            File file = new File(model.getFileModel().getDataDir(), key);
            if ((file.exists()) && (file.list().length == 0))
                file.delete();
        }
    }

    private void download1(CamelContext camelContext, FTPModel ftpModel) throws InitUriException, ConnectionException, FileOperationException, IOException {
        Endpoint endpoint = camelContext.getEndpoint(names.get(FROM_SCAN_DIR_NAME_URI));
        IFileOperations operations;
        try {
            ftpModel.setEndpoint(endpoint);
            operations = TOperations.getOperations(ftpModel);
        } catch (Exception e) {
            String err = LogProxy.assembly("eSign", names.get(NAME), "FAILED", ftpModel.getGlobal(), UUID.randomUUID(), new StringBuilder().append("O2P Initial SCAN URI Failed. Please check URI. ").append(e.getMessage()).toString());
            throw new InitUriException(err);
        }
        RemoteFileConfiguration remoteFileConfiguration = operations.getRemoteFileConfiguration();
        ftpModel.setSrcRemoteFileConfiguration(remoteFileConfiguration);

        boolean flag = false;
        try {
            flag = operations.connection(remoteFileConfiguration);
        } catch (GenericFileOperationFailedException e) {
            String err = LogProxy.assembly("eSign", names.get(NAME), "FAILED", ftpModel.getGlobal(), UUID.randomUUID(), new StringBuilder().append("O2P conncet SCAN URI Failed. Please check URI File Server is running? ").append(e.getMessage()).toString());
            throw new ConnectionException(err);
        }
        pushOldFile(ftpModel);
        if (flag) {
            try {
                operations.jumpCurrentDirectory(new StringBuilder().append(File.separator).append(remoteFileConfiguration.getDirectory()).toString());
            } catch (GenericFileOperationFailedException e) {
                String err = LogProxy.assembly("eSign", names.get(NAME), "FAILED", ftpModel.getGlobal(), UUID.randomUUID(), new StringBuilder().append("O2P change SCAN path Failed. Please check URI connection. ").append(operations.getUserDirectory()).append(File.separator).append(remoteFileConfiguration.getDirectory()).append(" is exsist? ").append(e.getMessage()).toString());
                throw new FileOperationException(err);
            }
            ftpModel.setSuffix(SUFFIX);
            ftpModel.setSuffix_length(SUFFIX_LENGTH);
            TOperations.getFile(ftpModel);
        } else {
            String err = LogProxy.assembly("eSign", names.get(NAME), "FAILED", ftpModel.getGlobal(), UUID.randomUUID(), "O2P conncet SCAN URI Failed. Please check URI File Server is running? ");
            throw new ConnectionException(err);
        }
    }

    private void mapping(CamelContext camelContext, FTPModel ftpModel)
            throws Exception {
        Endpoint endpoint2 = camelContext.getEndpoint(names.get(FROM_SCANED_DIR_NAME_URI));
        ftpModel.setEndpoint(endpoint2);
        ftpModel.setSuffix(SSUFFIX);
        ftpModel.setSuffix_length(SSUFFIX_LENGTH);
        TOperations.mapping(ftpModel);
    }

    private void accross1(int size) {
    }

    private void accoss2(FTPModel ftpModel)
            throws IOException {
        int count = 0;
        Map _download_ = new HashMap();
        Map _tables_ = ftpModel.get_tables_();
        String _project_ = ftpModel.getProject();
        UUID global = ftpModel.getGlobal();
        Map rule = ftpModel.getRules();
        PatternModel patternModels = ftpModel.getPatternModel();
        List outputModels = patternModels.getVals();
        String outputSplit = patternModels.getSub();
        CTRModel.CTRSchema invaild = ftpModel.getCtrModel().getInvaild();
        String SPLIT = (invaild.getSplit().get(0)).getTrue(null);

        Iterator iterator = _tables_.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String date = (String) entry.getKey();
            FBufferModel fBufferModel = (FBufferModel) rule.get(new StringBuilder().append(date).append(global).toString());
            if (fBufferModel == null) {
                StringBuilder _failed_ctr_ = new StringBuilder("");
                BufferedWriter _failed_buffer_writer_ = null;

                Map _maps_ = (Map) entry.getValue();
                TOperations.assemblyFile(outputModels, _failed_ctr_, count, outputSplit);

                File _target_file_ = new File(ftpModel.getFileModel().getFailedDir(), _failed_ctr_.append(names.get(FAILED_EXT)).toString());
                File _failed_file_ = new File(new StringBuilder().append(_target_file_).append(".inprogress").toString());
                _failed_ctr_.setLength(0);
                TOperations.assemblyHead(invaild.getHead(), _failed_ctr_, SPLIT);
                try {
                    int _failed_count_ = 0;
                    _failed_buffer_writer_ = TIO.create(_failed_file_);
                    Iterator iter = _maps_.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry2 = (Map.Entry) iter.next();
                        String key = (String) entry2.getKey();
                        FileLinkedModel fileLinkedModel = (FileLinkedModel) entry2.getValue();
                        File _f_ = fileLinkedModel.getFile();
                        if (_f_.delete()) {
                            List ns = fileLinkedModel.getSubNames();
                            String err = LogProxy.assembly("eSign", _project_, "SUCCESS", ftpModel.getGlobal(), UUID.randomUUID(), new StringBuilder().append("O2P deleted file ").append(_f_.getAbsolutePath()).toString());
                            LOGGER.debug(err);
                            TOperations.assemblyBody(invaild.getBody(), _failed_ctr_, ns, "", fileLinkedModel.getTrueName(), SPLIT);
                            _failed_count_++;
                            if (0 == _failed_count_ % 256) {
                                TIO.append(_failed_buffer_writer_, _failed_ctr_.toString());
                                _failed_ctr_.setLength(0);
                            }
                        }
                    }
                    TOperations.assemblyTail(invaild.getTail(), _failed_ctr_, _failed_count_, SPLIT);
                    TIO.append(_failed_buffer_writer_, _failed_ctr_);
                    FileUtil.renameFile(_failed_file_, _target_file_, true);
                } catch (IOException e) {
                } finally {
                    TIO.close(_failed_buffer_writer_);
                    _failed_ctr_.setLength(0);
                }
            } else {
                Map _hashmap_ = (Map) _tables_.get(date);
                File file = fBufferModel.getFile();
                ftpModel.setUniqId(count);
                EsignAction esignAction = new ReceiveBillZipAction(ftpModel, ftpModel.getOperations(), date, _hashmap_, file, _download_, names.get(SUCCESS_EXT), names.get(FAILED_EXT));
                esignAction.execute();
                file.delete();
                count++;
            }
        }
    }

    private void push1(CamelContext camelContext, FTPModel ftpModel)
            throws ConnectionException, FileOperationException, InitUriException {
        Endpoint endpoint3 = camelContext.getEndpoint(names.get(ERROR_Dir));
        IFileOperations operations = TOperations.getOperations(endpoint3, ftpModel);
        ftpModel.setOperations(operations);
        String _false_ext_ = names.get(FAILED_EXT);
        int _false_length_ = _false_ext_.length();

        for (File _f_ : ftpModel.getFileModel().getFailedDir().listFiles()) {
            File lock = new File(new StringBuilder().append(_f_).append(".lock").toString());
            try {
                String name = _f_.getName();
                if (!FileUtil.createNewFile(lock)) {
                    lock.delete();
                } else if (_false_ext_.equalsIgnoreCase(name.substring(name.length() - _false_length_)))
                    operations.storeFile(_f_);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.delete();
            }
        }
    }

    private void push2(CamelContext camelContext, FTPModel ftpModel)
            throws ConnectionException, FileOperationException, InitUriException {
        boolean flag = false;
        String _compress_ext_ = ".tar.gz";
        Endpoint endpoint4 = camelContext.getEndpoint(names.get(TO_URI));
        IFileOperations operations = TOperations.getOperations(endpoint4, ftpModel);
        ftpModel.setOperations(operations);
        int _compress_length_ = _compress_ext_.length();

        for (File _f_ : ftpModel.getFileModel().getPutDir().listFiles()) {
            File lock = new File(new StringBuilder().append(_f_).append(".lock").toString());
            try {
                flag = FileUtil.createNewFile(lock);
                if (!flag) {
                    lock.delete();
                } else {
                    String name = _f_.getName();
                    if (_compress_ext_.equalsIgnoreCase(name.substring(name.length() - _compress_length_)))
                        operations.storeFile(_f_);
                }
            } catch (IOException e) {
            } finally {
                lock.delete();
            }
        }
    }

    private void pushOldFile(FTPModel model)
            throws IOException {
        Map _tables_ = model.get_tables_();
        Map _scans_ = model.get_scans_();
        String scan_suffix = SUFFIX.substring(1);
        String scaned_suffix = SSUFFIX.substring(1);
        PatternModel patternModel = model.getIndexs();
        for (File file : model.getFileModel().getDataDir().listFiles())
            if (file.isDirectory()) {
                Map _hashmap_ = new HashMap();
                Map _hashmap2_ = new HashMap();
                String date = file.getName();
                for (File f : file.listFiles()) {
                    String name = f.getName();
                    int end = name.lastIndexOf(".") + 1;
                    if ((!name.substring(end).equals("lock")) && (!name.substring(end).equals("inprogress"))) {
                        File lock = new File(new StringBuilder().append(f).append(".lock").toString());
                        File inprogress = new File(new StringBuilder().append(f).append(".inprogress").toString());
                        if ((!lock.exists()) && (!inprogress.exists())) {
                            FileLinkedModel fileLinkedModel = new FileLinkedModel();
                            List ns = TString.splitSimpleString(name, patternModel.getSub(), patternModel.getVals(), null);
                            String newStr = (String) ns.get(ns.size() - 1);
                            TOperations.assemblyFileLinkedModel(fileLinkedModel, name, ns, f, patternModel.getSub());
                            if (lock.createNewFile()) {
                                if (name.substring(end).equalsIgnoreCase(scan_suffix)) {
                                    _hashmap_.put(newStr, fileLinkedModel);
                                } else if (name.substring(end).equalsIgnoreCase(scaned_suffix)) {
                                    _hashmap2_.put(newStr, fileLinkedModel);
                                } else {
                                    lock.delete();
                                }
                            }
                        }
                    } else {//   .lock or .inprogress file  mtime>2 date   to delete  .lock or .inprogress sign
//                        if ((name.substring(end).equals("lock") || name.substring(end).equals("inprogress"))
//                                && TTimestamp.isDeprecated(file, Byte.valueOf(names.get(DELETE_LOCK_INPROGRESS_TYPE)), Integer.valueOf(names.get(DELETE_LOCK_INPROGRESS_DATE)))) {
//                            f.delete();
//                        }
                    }
                }
                if (_hashmap_.size() > 0) {
                    _tables_.put(date, _hashmap_);
                }
                if (_hashmap2_.size() > 0)
                    _scans_.put(date, _hashmap2_);
            }
    }

    private void deleteDepractedMapping(FTPModel model) {
        File mapping = model.getFileModel().getMappingDir();
        if ((mapping.exists()) && (mapping.isDirectory())) {
            File[] files = mapping.listFiles();
            for (File file : files)
                if (TTimestamp.isDeprecated(file, model.getTimeModel().getDeprecatedDeleteType(), model.getTimeModel().getDeprecatedDelete())) {
                    LOGGER.debug("Delete Deprecated Mapping {}", file.getName());
                    FileUtil.deleteFile(file);
                }
        }
    }
}