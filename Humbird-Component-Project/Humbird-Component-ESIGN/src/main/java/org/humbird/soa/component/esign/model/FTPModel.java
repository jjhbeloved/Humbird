package org.humbird.soa.component.esign.model;

import org.apache.camel.Endpoint;
import org.apache.camel.component.file.remote.RemoteFileConfiguration;
import org.humbird.soa.common.model.common.FBufferModel;
import org.humbird.soa.common.model.common.PatternModel;
import org.humbird.soa.component.esign.tools.IFileOperations;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;

/**
 * Created by david on 15/4/7.
 */
public class FTPModel {
    private UUID global;
    private String project;
    private Map<String, Map<String, FileLinkedModel>> _tables_;
    private Map<String, Map<String, FileLinkedModel>> _scans_;
    private Endpoint endpoint;
    private IFileOperations operations;
    private RemoteFileConfiguration srcRemoteFileConfiguration;
    private RemoteFileConfiguration tarRemoteFileConfiguration;
    private Map<String, StringBuilder> _lines_;
    private Map<String, Integer> _counts_;
    private Map<String, Boolean> _read_;
    private long time;
    private String suffix;
    private int suffix_length;
    private List<File> mapping_lock;
    private Logger LOGGER;
    private int uniqId;
    private FileModel fileModel;
    private TimeModel timeModel;
    private StatModel statModel;
    private PatternModel patternModel;
    private PatternModel indexs;
    private DocDateModel docDateModel;
    private CTRModel ctrModel;
    private Map<String, FBufferModel> rules = new HashMap();

    private List<String> dels = new ArrayList();

    public FTPModel() {
    }

    public FTPModel(UUID global, String project, int suffix_length) {
        this.global = global;
        this.project = project;
        this.suffix_length = suffix_length;
    }

    public FTPModel(Endpoint endpoint, IFileOperations operations, RemoteFileConfiguration srcRemoteFileConfiguration, RemoteFileConfiguration tarRemoteFileConfiguration, Map<String, StringBuilder> _lines_, Map<String, Integer> _counts_, Map<String, Boolean> _read_, long time, int suffix_length) {
        this.endpoint = endpoint;
        this.operations = operations;
        this.srcRemoteFileConfiguration = srcRemoteFileConfiguration;
        this.tarRemoteFileConfiguration = tarRemoteFileConfiguration;
        this._lines_ = _lines_;
        this._counts_ = _counts_;
        this._read_ = _read_;
        this.time = time;
        this.suffix_length = suffix_length;
    }

    public UUID getGlobal() {
        return this.global;
    }

    public String getProject() {
        return this.project;
    }

    public Map<String, Map<String, FileLinkedModel>> get_tables_() {
        return this._tables_;
    }

    public void set_tables_(Map<String, Map<String, FileLinkedModel>> _tables_) {
        this._tables_ = _tables_;
    }

    public Map<String, Map<String, FileLinkedModel>> get_scans_() {
        return this._scans_;
    }

    public void set_scans_(Map<String, Map<String, FileLinkedModel>> _scans_) {
        this._scans_ = _scans_;
    }

    public Endpoint getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public IFileOperations getOperations() {
        return this.operations;
    }

    public void setOperations(IFileOperations operations) {
        this.operations = operations;
    }

    public RemoteFileConfiguration getSrcRemoteFileConfiguration() {
        return this.srcRemoteFileConfiguration;
    }

    public void setSrcRemoteFileConfiguration(RemoteFileConfiguration srcRemoteFileConfiguration) {
        this.srcRemoteFileConfiguration = srcRemoteFileConfiguration;
    }

    public RemoteFileConfiguration getTarRemoteFileConfiguration() {
        return this.tarRemoteFileConfiguration;
    }

    public void setTarRemoteFileConfiguration(RemoteFileConfiguration tarRemoteFileConfiguration) {
        this.tarRemoteFileConfiguration = tarRemoteFileConfiguration;
    }

    public Map<String, StringBuilder> get_lines_() {
        return this._lines_;
    }

    public void set_lines_(Map<String, StringBuilder> _lines_) {
        this._lines_ = _lines_;
    }

    public Map<String, Integer> get_counts_() {
        return this._counts_;
    }

    public void set_counts_(Map<String, Integer> _counts_) {
        this._counts_ = _counts_;
    }

    public Map<String, Boolean> get_read_() {
        return this._read_;
    }

    public void set_read_(Map<String, Boolean> _read_) {
        this._read_ = _read_;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getSuffix_length() {
        return this.suffix_length;
    }

    public void setSuffix_length(int suffix_length) {
        this.suffix_length = suffix_length;
    }

    public List<File> getMapping_lock() {
        return this.mapping_lock;
    }

    public void setMapping_lock(List<File> mapping_lock) {
        this.mapping_lock = mapping_lock;
    }

    public Logger getLOGGER() {
        return this.LOGGER;
    }

    public void setLOGGER(Logger LOGGER) {
        this.LOGGER = LOGGER;
    }

    public int getUniqId() {
        return this.uniqId;
    }

    public void setUniqId(int uniqId) {
        this.uniqId = uniqId;
    }

    public FileModel getFileModel() {
        return this.fileModel;
    }

    public void setFileModel(FileModel fileModel) {
        this.fileModel = fileModel;
    }

    public TimeModel getTimeModel() {
        return this.timeModel;
    }

    public void setTimeModel(TimeModel timeModel) {
        this.timeModel = timeModel;
    }

    public StatModel getStatModel() {
        return this.statModel;
    }

    public void setStatModel(StatModel statModel) {
        this.statModel = statModel;
    }

    public Map<String, FBufferModel> getRules() {
        return this.rules;
    }

    public void setRules(Map<String, FBufferModel> rules) {
        this.rules = rules;
    }

    public PatternModel getPatternModel() {
        return this.patternModel;
    }

    public void setPatternModel(PatternModel patternModel) {
        this.patternModel = patternModel;
    }

    public PatternModel getIndexs() {
        return this.indexs;
    }

    public DocDateModel getDocDateModel() {
        return this.docDateModel;
    }

    public void setDocDateModel(DocDateModel docDateModel) {
        this.docDateModel = docDateModel;
    }

    public void setIndexs(PatternModel indexs) {
        this.indexs = indexs;
    }

    public CTRModel getCtrModel() {
        return this.ctrModel;
    }

    public void setCtrModel(CTRModel ctrModel) {
        this.ctrModel = ctrModel;
    }

    public List<String> getDels() {
        return this.dels;
    }

    public void clear() {
        this._read_.clear();
        this._counts_.clear();
        this._lines_.clear();
        this._scans_.clear();
        this._tables_.clear();
    }
}
