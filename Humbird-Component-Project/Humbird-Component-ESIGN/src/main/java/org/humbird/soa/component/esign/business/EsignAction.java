package org.humbird.soa.component.esign.business;

import org.humbird.soa.common.tools.TIO;
import org.humbird.soa.component.esign.model.CTRModel;
import org.humbird.soa.component.esign.model.FTPModel;
import org.humbird.soa.component.esign.model.FileLinkedModel;
import org.humbird.soa.component.esign.tools.IFileOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by david on 15/4/7.
 */
public abstract class EsignAction implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsignAction.class);

    protected IFileOperations operations;

    protected Map<String, FileLinkedModel> _datas_;

    protected File _template_file_;

    protected String _date_;

    protected String _success_ext_;

    protected String _failed_ext_;

    protected Map<String, List> _download_;

    protected FTPModel model;

    protected CTRModel.CTRSchema invaild;

    protected CTRModel.CTRSchema vaild;

    protected List<File> _fs_ = new ArrayList();

    protected List<File> _failed_fs_ = new ArrayList();

    protected File _temp_date_;

    protected boolean hasDone = true;

    protected int id = 999;

    public EsignAction(FTPModel model, IFileOperations operations, String _date_, Map<String, FileLinkedModel> _datas_, File _template_file_, Map<String, List> _download_, String _success_ext_, String _failed_ext_) {
        this.operations = operations;
        this._date_ = _date_;
        this._datas_ = _datas_;
        this._template_file_ = _template_file_;
        this._download_ = _download_;
        this._success_ext_ = _success_ext_;
        this._failed_ext_ = _failed_ext_;
        this.vaild = model.getCtrModel().getVaild();
        this.invaild = model.getCtrModel().getInvaild();
        this.model = model;
    }

    public void execute() {
        int count = 0;
        while (hasDone) {
            try {
                beforeMethod();
                invoke();
                afterMethod();
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            } finally {
                TIO.releaseLock(this._fs_);
                TIO.releaseLock(this._failed_fs_);
                remove();
            }
            count++;
            if(count > 5) {
                break;
            }
        }
    }

    protected abstract void invoke()
            throws IOException;

    protected abstract void beforeMethod();

    protected abstract void afterMethod()
            throws Exception;

    protected abstract void remove();
}
