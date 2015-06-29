package org.humbird.soa.common.model.o2p.log;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by david on 15/1/1.
 */
public class ContractInteraction implements Serializable, Cloneable {

    private String contractInteractionId;
    private String bizFuncCode;
    private String bizIntfCode;
    private String contractVersion;
    private String regSerCode;
    private String regSerVersion;
    private String srcTranId;
    private String dstTranId;
    private String serviceLevel;
    private String srcOrgCode;
    private String dstOrgCode;
    private String srcSysCode;
    private String dstSysCode;
    private String testFlag;
    private Timestamp srcReqTime;
    private Timestamp centerRecReqTime;
    private Timestamp centerFwd2DstTime;
    private Timestamp dstRecTime;
    private Timestamp dstReplyTime;
    private Timestamp centerRecDstTime;
    private Timestamp centerFwd2SrcTime;
    private Timestamp srcConfirmTime;
    private Timestamp srcResponseTime;
    private String responseType;
    private String responseCode;
    private String responseDes;
    private Timestamp createTime;
    private String tabSuffix;

    //来源实例标识
    private String srcSysSign;

    public String getSrcSysSign() {
        return srcSysSign;
    }

    public void setSrcSysSign(String srcSysSign) {
        this.srcSysSign = srcSysSign;
    }

    public void setContractInteractionId(String contractInteractionId) {
        this.contractInteractionId = contractInteractionId;
    }

    public String getContractInteractionId() {
        return this.contractInteractionId;
    }

    public void setBizFuncCode(String bizFuncCode) {
        this.bizFuncCode = bizFuncCode;
    }

    public String getBizFuncCode() {
        return this.bizFuncCode;
    }

    public void setBizIntfCode(String bizIntfCode) {
        this.bizIntfCode = bizIntfCode;
    }

    public String getBizIntfCode() {
        return this.bizIntfCode;
    }

    public void setContractVersion(String contractVersion) {
        this.contractVersion = contractVersion;
    }

    public String getContractVersion() {
        return this.contractVersion;
    }

    public void setRegSerCode(String regSerCode) {
        this.regSerCode = regSerCode;
    }

    public String getRegSerCode() {
        return this.regSerCode;
    }

    public void setRegSerVersion(String regSerVersion) {
        this.regSerVersion = regSerVersion;
    }

    public String getRegSerVersion() {
        return this.regSerVersion;
    }

    public void setSrcTranId(String srcTranId) {
        this.srcTranId = srcTranId;
    }

    public String getSrcTranId() {
        return this.srcTranId;
    }

    public void setDstTranId(String dstTranId) {
        this.dstTranId = dstTranId;
    }

    public String getDstTranId() {
        return this.dstTranId;
    }

    public void setServiceLevel(String serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

    public String getServiceLevel() {
        return this.serviceLevel;
    }

    public void setSrcOrgCode(String srcOrgCode) {
        this.srcOrgCode = srcOrgCode;
    }

    public String getSrcOrgCode() {
        return this.srcOrgCode;
    }

    public void setDstOrgCode(String dstOrgCode) {
        this.dstOrgCode = dstOrgCode;
    }

    public String getDstOrgCode() {
        return this.dstOrgCode;
    }

    public void setSrcSysCode(String srcSysCode) {
        this.srcSysCode = srcSysCode;
    }

    public String getSrcSysCode() {
        return this.srcSysCode;
    }

    public void setDstSysCode(String dstSysCode) {
        this.dstSysCode = dstSysCode;
    }

    public String getDstSysCode() {
        return this.dstSysCode;
    }

    public void setTestFlag(String testFlag) {
        this.testFlag = testFlag;
    }

    public String getTestFlag() {
        return this.testFlag;
    }

    public void setSrcReqTime(Timestamp srcReqTime) {
        this.srcReqTime = srcReqTime;
    }

    public Timestamp getSrcReqTime() {
        return this.srcReqTime;
    }

    public void setCenterRecReqTime(Timestamp centerRecReqTime) {
        this.centerRecReqTime = centerRecReqTime;
    }

    public Timestamp getCenterRecReqTime() {
        return this.centerRecReqTime;
    }

    public void setCenterFwd2DstTime(Timestamp centerFwd2DstTime) {
        this.centerFwd2DstTime = centerFwd2DstTime;
    }

    public Timestamp getCenterFwd2DstTime() {
        return this.centerFwd2DstTime;
    }

    public void setDstRecTime(Timestamp dstRecTime) {
        this.dstRecTime = dstRecTime;
    }

    public Timestamp getDstRecTime() {
        return this.dstRecTime;
    }

    public void setDstReplyTime(Timestamp dstReplyTime) {
        this.dstReplyTime = dstReplyTime;
    }

    public Timestamp getDstReplyTime() {
        return this.dstReplyTime;
    }

    public void setCenterRecDstTime(Timestamp centerRecDstTime) {
        this.centerRecDstTime = centerRecDstTime;
    }

    public Timestamp getCenterRecDstTime() {
        return this.centerRecDstTime;
    }

    public void setCenterFwd2SrcTime(Timestamp centerFwd2SrcTime) {
        this.centerFwd2SrcTime = centerFwd2SrcTime;
    }

    public Timestamp getCenterFwd2SrcTime() {
        return this.centerFwd2SrcTime;
    }

    public void setSrcConfirmTime(Timestamp srcConfirmTime) {
        this.srcConfirmTime = srcConfirmTime;
    }

    public Timestamp getSrcConfirmTime() {
        return this.srcConfirmTime;
    }

    public void setSrcResponseTime(Timestamp srcResponseTime) {
        this.srcResponseTime = srcResponseTime;
    }

    public Timestamp getSrcResponseTime() {
        return this.srcResponseTime;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseType() {
        return this.responseType;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseCode() {
        return this.responseCode;
    }

    public void setResponseDes(String responseDes) {
        this.responseDes = responseDes;
    }

    public String getResponseDes() {
        return this.responseDes;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getCreateTime() {
        return this.createTime;
    }

    public String getTabSuffix() {
        return tabSuffix;
    }

    public void setTabSuffix(String tabSuffix) {
        this.tabSuffix = tabSuffix;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
