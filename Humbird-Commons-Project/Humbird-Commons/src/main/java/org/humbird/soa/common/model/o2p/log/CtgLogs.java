package org.humbird.soa.common.model.o2p.log;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by david on 15/1/1.
 */
public class CtgLogs implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private String logsSeq;
    private String contractInteractionId;
    private String errCode;
    private String funName;
    private Timestamp createDate;
    private String errorMsg;
    private String descriptor;
    private String status;
    private String tabSuffix;
    private String srcSysSign;

    public String getSrcSysSign() {
        return srcSysSign;
    }

    public void setSrcSysSign(String srcSysSign) {
        this.srcSysSign = srcSysSign;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }

    public String getLogsSeq() {
        return logsSeq;
    }

    public void setLogsSeq(String logsSeq) {
        this.logsSeq = logsSeq;
    }

    public void setContractInteractionId(String contractInteractionId) {
        this.contractInteractionId = contractInteractionId;
    }

    public String getContractInteractionId() {
        return this.contractInteractionId;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrCode() {
        return this.errCode;
    }

    public void setFunName(String funName) {
        this.funName = funName;
    }

    public String getFunName() {
        return this.funName;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Timestamp getCreateDate() {
        return this.createDate;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public String getTabSuffix() {
        return tabSuffix;
    }

    public void setTabSuffix(String tabSuffix) {
        this.tabSuffix = tabSuffix;
    }
}