package org.humbird.soa.component.esign.business.impl;

import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.humbird.soa.common.utils.FileUtil;
import org.humbird.soa.common.compress.*;
import org.humbird.soa.common.io.FileReaderExt;
import org.humbird.soa.common.model.common.MapModel;
import org.humbird.soa.common.model.common.PatternModel;
import org.humbird.soa.common.tools.TFile;
import org.humbird.soa.common.tools.TIO;
import org.humbird.soa.common.tools.TString;
import org.humbird.soa.component.esign.business.EsignAction;
import org.humbird.soa.component.esign.log.LogProxy;
import org.humbird.soa.component.esign.model.CTRModel;
import org.humbird.soa.component.esign.model.FTPModel;
import org.humbird.soa.component.esign.model.FileLinkedModel;
import org.humbird.soa.component.esign.model.OutputCallback;
import org.humbird.soa.component.esign.tools.IFileOperations;
import org.humbird.soa.component.esign.tools.TOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by david on 15/4/7.
 */
public class ReceiveBillZipAction extends EsignAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveBillZipAction.class);
    private File _success_file_;
    private File _failed_file_;
    private BufferedWriter _success_buffer_writer_ = null;
    private BufferedWriter _failed_buffer_writer_ = null;
    private int _success_count_ = 0;
    private int _failed_count_ = 0;
    private String vaildSplit;
    private String invaildSplit;

    public ReceiveBillZipAction(FTPModel model, IFileOperations operations1, String _date_, Map<String, FileLinkedModel> _datas_, File _template_file_, Map<String, List> _download_, String _success_ext_, String _failed_ext_) {
        super(model, operations1, _date_, _datas_, _template_file_, _download_, _success_ext_, _failed_ext_);
    }

    protected void beforeMethod() {
        this._temp_date_ = new File(this.model.getFileModel().getDataDir(), this._date_);
        File _temp_failed_ = this.model.getFileModel().getFailedDir();
        if (!this._temp_date_.exists()) this._temp_date_.mkdirs();
        if (!_temp_failed_.exists()) _temp_failed_.mkdirs();

        PatternModel patternModels = this.model.getPatternModel();
        List list = patternModels.getVals();
        StringBuilder _success_ctr_ = new StringBuilder("");
        int i = 0;
        for (int size = list.size(); i < size; i++) {
            MapModel mapModel = (MapModel) list.get(i);
            _success_ctr_.append(mapModel.getTrue(new OutputCallback(this.model.getUniqId())));
            if (i < size - 1) {
                _success_ctr_.append(patternModels.getSub());
            }
        }
        String fname = _success_ctr_.toString();
        this._success_file_ = new File(this._temp_date_, new StringBuilder().append(fname).append(this._success_ext_).append(".inprogress").toString());

        if (this._success_file_.exists()) {
            int end = _success_ctr_.length() - 3;
            while (true) {
                this._success_file_ = new File(this._temp_date_, new StringBuilder().append(_success_ctr_.substring(0, end)).append(id).append(this._success_ext_).append(".inprogress").toString());
                if (!this._success_file_.exists()) {
                    break;
                }
                id--;
            }
        }

        this._failed_file_ = new File(_temp_failed_, new StringBuilder().append(fname).append(this._failed_ext_).append(".inprogress").toString());
    }

    protected void invoke() throws IOException {
        head();
        body();
        tail();
    }

    protected void afterMethod()
            throws Exception {
        File _err_file_;
        if ((_err_file_ = rename(this._failed_count_, this._failed_file_)) != null)
            this._failed_fs_.add(_err_file_);
        File _suc_file_;
        if ((_suc_file_ = rename(this._success_count_, this._success_file_)) != null) {
            this._fs_.add(_suc_file_);
            this._success_file_ = _suc_file_;

            IArchive iArchive = new TarArchive();
            ICompress iCompress = new GzipCompress();
            String _name_ = _suc_file_.getName();
            File _data_ = this.model.getFileModel().getDataDir();
            try {
                _name_ = iArchive.archive(this._fs_, _data_.getAbsolutePath(), _name_.substring(0, _name_.length() - this._success_ext_.length()), true);
            } catch (Exception e) {
                String err = new StringBuilder().append("O2P Archive(tar -xvf xxx) file Failed. Please check. ").append(e.getMessage()).toString();
                throw new Exception(err);
            }
            try {
                iCompress.compress(_name_, true);
            } catch (CompressException e) {
                File tar = this.model.getFileModel().getTarDir();
                File src = new File(_name_);
                String err = new StringBuilder().append("O2P Compress(gz xxx) file Failed. Please check. ").append(e.getMessage()).toString();
                try {
                    FileUtil.renameFile(src, tar, true);
                } catch (IOException e1) {
                    err = new StringBuilder().append("O2P Compress(gz xxx) file Failed, and archive file Failed, loss all file. Please check. ").append(e1.getMessage()).toString();
                }
                throw new Exception(err);
            }

            File _src_ = new File(new StringBuilder().append(_name_).append(".gz").toString());
            File _put_ = this.model.getFileModel().getPutDir();
            try {
                FileUtil.renameFile(_src_, new File(_put_, _src_.getName()), true);
            } catch (IOException e1) {
                File tar = this.model.getFileModel().getGzDir();
                String err = new StringBuilder().append("O2P move .tar.gz file Failed. Please check. ").append(e1.getMessage()).toString();
                try {
                    FileUtil.renameFile(_src_, tar, true);
                } catch (IOException e2) {
                    err = new StringBuilder().append("O2P move .tar.gz file Failed, and archive file Failed, loss all file. Please check. ").append(e2.getMessage()).toString();
                }
                throw new Exception(err);
            }
        }
    }

    private void head() throws IOException {
        assembly(this.vaild, true);
        assembly(this.invaild, false);
    }

    private void body() throws IOException {
        int _size_ = this._datas_.size();
        StringBuilder _sb_ = new StringBuilder("");
        boolean flag = true;
        boolean overLimit = true;   //  tar.gz file limit
        int _suffix_size_ = this.model.getSuffix().length();
        Map _scans_ = (Map) this.model.get_scans_().get(this._date_);

        if (_scans_ != null) {
            Iterator iterator = _scans_.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                FileLinkedModel scanFileLinkedModel = (FileLinkedModel) entry.getValue();
                FileLinkedModel fileLinkedModel = null;
                if ((fileLinkedModel = this._datas_.get(name)) != null) {
                    List names = scanFileLinkedModel.getSubNames();
                    TOperations.assemblyBody(this.vaild.getBody(), _sb_, names, scanFileLinkedModel.getTrueName(), fileLinkedModel.getTrueName(), this.vaildSplit);
                    this._success_count_ += 1;
                    this._fs_.add(scanFileLinkedModel.getFile());
                    this._fs_.add(fileLinkedModel.getFile());
                    this._datas_.remove(name);
                    if (TIO.write(this._success_buffer_writer_, _sb_, this._success_count_)) {
                        _sb_.setLength(0);
                    }
                }
                if (_size_ == this._success_count_) {
                    flag = false;
                    hasDone = false;
                    break;
                } else if(this._success_count_ == 50) {
                    flag = false;
                    overLimit = false;
                    break;
                }
            }
        }

        PatternModel patternModel = this.model.getIndexs();
        FileReaderExt _reader_ = null;
        try {
            _reader_ = new FileReaderExt(this._template_file_, "UTF-8", TFile.NEW_LINE, " ");
            if (flag) {
                LOOP:
                for (List<String> _names_ : _reader_) {
                    for (String _name_ : _names_) {
                        List splits = TString.splitSimpleString(_name_, patternModel.getSub(), patternModel.getVals(), null);
                        String _tmp_name_ = (String) splits.get(splits.size() - 1);
                        FileLinkedModel fileLinkedModel = null;
                        if ((fileLinkedModel = this._datas_.get(_tmp_name_)) != null) {
                            List names = fileLinkedModel.getSubNames();
                            this._datas_.remove(_tmp_name_);
                            if (this.operations.retrieveFile(_name_, this._temp_date_)) {
                                TOperations.assemblyBody(this.vaild.getBody(), _sb_, names, _name_, fileLinkedModel.getTrueName(), this.vaildSplit);
                                this._success_count_ += 1;
                                this._fs_.add(new File(this._temp_date_, _name_));
                                this._fs_.add(fileLinkedModel.getFile());
                                if (TIO.write(this._success_buffer_writer_, _sb_, this._success_count_)) {
                                    _sb_.setLength(0);
                                }
                            }
                        }
                        if (_size_ == this._success_count_) {
                            hasDone = false;
                            break LOOP;
                        } else if(this._success_count_ == 50) {
                            overLimit = false;
                            break LOOP;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (UnsupportedEncodingException e) {
            throw e;
        } finally {
            if (_reader_ != null)
                try {
                    _reader_.close();
                } catch (IOException e) {
                }
        }
        TIO.append(this._success_buffer_writer_, _sb_);
        _sb_.setLength(0);

        if(overLimit) {
            Iterator iterator = this._datas_.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                FileLinkedModel fileLinkedModel = (FileLinkedModel) entry.getValue();
                File _src_file_ = fileLinkedModel.getFile();
                List names = fileLinkedModel.getSubNames();
                if (_src_file_.delete()) {
                    String err = LogProxy.assembly("eSign", this.model.getProject(), "SUCCESS", this.model.getGlobal(), UUID.randomUUID(), new StringBuilder().append("O2P deleted file ").append(_src_file_.getAbsolutePath()).toString());
                    LOGGER.debug(err);
                    TOperations.assemblyBody(this.invaild.getBody(), _sb_, names, "", fileLinkedModel.getTrueName(), this.invaildSplit);
                    this._failed_count_ += 1;
                    this._failed_fs_.add(_src_file_);
                    if (TIO.write(this._failed_buffer_writer_, _sb_, this._failed_count_)) {
                        _sb_.setLength(0);
                    }
                }
            }
        }

        TIO.append(this._failed_buffer_writer_, _sb_);
        _sb_.setLength(0);
    }

    private void tail() throws IOException {
        StringBuilder _sb_ = new StringBuilder("");
        TOperations.assemblyTail(this.vaild.getTail(), _sb_, this._success_count_, this.vaildSplit);
        TIO.append(this._success_buffer_writer_, _sb_);
        _sb_.setLength(0);
        TOperations.assemblyTail(this.invaild.getTail(), _sb_, this._failed_count_, this.invaildSplit);
        TIO.append(this._failed_buffer_writer_, _sb_);
        _sb_.setLength(0);
    }

    public void run() {
        execute();
    }

    protected void remove() {
        FileUtil.deleteFile(this._success_file_);
        FileUtil.deleteFile(this._failed_file_);
        TIO.close(this._success_buffer_writer_);
        TIO.close(this._failed_buffer_writer_);
        if ((this._temp_date_.exists()) && (this._temp_date_.list().length == 0)) {
            FileUtil.deleteFile(this._temp_date_);
        }
        _success_count_ = 0;
        _failed_count_ = 0;
    }

    private File rename(int count, File _file_) {
        if (count == 0) {
            _file_.delete();
            return null;
        }
        String _fname_ = _file_.getAbsolutePath();
        File _local_file_ = new File(_fname_.substring(0, _fname_.length() - ".inprogress".length()));
        File lock = new File(new StringBuilder().append(_local_file_).append(".lock").toString());
        try {
            try {
                lock.createNewFile();
            } catch (IOException e) {
            }
            if (FileUtil.renameFile(_file_, _local_file_, true)) ;
        } catch (IOException var16) {
            lock.delete();
            throw new GenericFileOperationFailedException(new StringBuilder().append("Cannot rename local work file from: ").append(_file_).append(" to: ").append(_local_file_).toString(), var16);
        }
        return _local_file_;
    }

    private void assembly(CTRModel.CTRSchema schema, boolean is) throws IOException {
        List list = schema.getSplit();
        StringBuilder stringBuilder = new StringBuilder("");
        String split = ((MapModel) list.get(0)).getTrue(null);
        BufferedWriter bufferedWriter = null;
        if (is) {
            this.vaildSplit = split;
            this._success_buffer_writer_ = TIO.create(this._success_file_);
            bufferedWriter = this._success_buffer_writer_;
        } else {
            this.invaildSplit = split;
            this._failed_buffer_writer_ = TIO.create(this._failed_file_);
            bufferedWriter = this._failed_buffer_writer_;
        }
        list = schema.getHead();
        int i = 0;
        for (int size = list.size(); i < size; i++) {
            MapModel mapModel = (MapModel) list.get(i);
            stringBuilder.append(mapModel.getTrue(null));
            if (i < size - 1) {
                stringBuilder.append(split);
            }
        }
        stringBuilder.append(TFile.NEW_LINE);

        TIO.append(bufferedWriter, stringBuilder);
        stringBuilder.setLength(0);
    }
}
