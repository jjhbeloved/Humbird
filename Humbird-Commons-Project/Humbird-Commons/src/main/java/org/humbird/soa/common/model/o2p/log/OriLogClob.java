package org.humbird.soa.common.model.o2p.log;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by david on 15/1/1.
 */
public class OriLogClob implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private String olcId;
    private String endpointInteractionId;
    private String contractInteractionId;
    private Long endPointId;
    private String srcTranId;
    private String srcOrgCode;
    private String srcSysCode;
    private String dstTranId;
    private String dstOrgCode;
    private String dstSysCode;
    private String srcIp;
    private String reqOrRes;
    private String msg;
    private Timestamp createTime;
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

    public String getOlcId() {
        return olcId;
    }

    public void setOlcId(String olcId) {
        this.olcId = olcId;
    }

    public String getEndpointInteractionId() {
        return endpointInteractionId;
    }

    public void setEndpointInteractionId(String endpointInteractionId) {
        this.endpointInteractionId = endpointInteractionId;
    }

    public void setContractInteractionId(String contractInteractionId) {
        this.contractInteractionId = contractInteractionId;
    }

    public String getContractInteractionId() {
        return this.contractInteractionId;
    }

    public Long getEndPointId() {
        return endPointId;
    }

    public void setEndPointId(Long endPointId) {
        this.endPointId = endPointId;
    }

    public void setSrcTranId(String srcTranId) {
        this.srcTranId = srcTranId;
    }

    public String getSrcTranId() {
        return this.srcTranId;
    }

    public void setSrcOrgCode(String srcOrgCode) {
        this.srcOrgCode = srcOrgCode;
    }

    public String getSrcOrgCode() {
        return this.srcOrgCode;
    }

    public void setSrcSysCode(String srcSysCode) {
        this.srcSysCode = srcSysCode;
    }

    public String getSrcSysCode() {
        return this.srcSysCode;
    }

    public void setDstTranId(String dstTranId) {
        this.dstTranId = dstTranId;
    }

    public String getDstTranId() {
        return this.dstTranId;
    }

    public void setDstOrgCode(String dstOrgCode) {
        this.dstOrgCode = dstOrgCode;
    }

    public String getDstOrgCode() {
        return this.dstOrgCode;
    }

    public void setDstSysCode(String dstSysCode) {
        this.dstSysCode = dstSysCode;
    }

    public String getDstSysCode() {
        return this.dstSysCode;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getSrcIp() {
        return this.srcIp;
    }

    public void setReqOrRes(String reqOrRes) {
        this.reqOrRes = reqOrRes;
    }

    public String getReqOrRes() {
        return this.reqOrRes;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
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
}
