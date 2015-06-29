package org.humbird.soa.common.model.o2p.log;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by david on 15/1/1.
 */
public class ExceptionLogs implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    //private Long exceptionInstId;
    private String exceptionInstId;
    private String exceptionSpecId;
    private String contractInteractionId;
    private String srcTranId;
    private Timestamp createTime;
    private String exceptionMessage;
    private String content;
    private String staffNbr;
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

    public String getExceptionInstId() {
        return exceptionInstId;
    }

    public void setExceptionInstId(String exceptionInstId) {
        this.exceptionInstId = exceptionInstId;
    }


    public void setExceptionSpecId(String exceptionSpecId) {
        this.exceptionSpecId = exceptionSpecId;
    }

    public String getExceptionSpecId() {
        return this.exceptionSpecId;
    }

    public void setContractInteractionId(String contractInteractionId) {
        this.contractInteractionId = contractInteractionId;
    }

    public String getContractInteractionId() {
        return this.contractInteractionId;
    }

    public void setSrcTranId(String srcTranId) {
        this.srcTranId = srcTranId;
    }

    public String getSrcTranId() {
        return this.srcTranId;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getCreateTime() {
        return this.createTime;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public void setStaffNbr(String staffNbr) {
        this.staffNbr = staffNbr;
    }

    public String getStaffNbr() {
        return this.staffNbr;
    }

    public String getTabSuffix() {
        return tabSuffix;
    }

    public void setTabSuffix(String tabSuffix) {
        this.tabSuffix = tabSuffix;
    }
}
