package org.humbird.soa.common.model.o2p.log;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by david on 15/1/1.
 */
public class EndpointInteraction implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private String endpointInteractionId;
    private String contractInteractionId;
    private Long serviceId;
    private Long endpointId;
    private Timestamp createDate;
    private String srcTranId;
    private String reqOrRes;
    private String queueMsgId;
    private Long resQueue;
    private Long reqQueue;
    private String dstTranId;
    private String dstOrgCode;
    private String dstSysCode;
    private Timestamp centerFwd2DstTime;
    private Timestamp dstReplyTime;
    private String descriptor;
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

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getServiceId() {
        return this.serviceId;
    }

    public void setEndpointId(Long endpointId) {
        this.endpointId = endpointId;
    }

    public Long getEndpointId() {
        return this.endpointId;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Timestamp getCreateDate() {
        return this.createDate;
    }

    public void setSrcTranId(String srcTranId) {
        this.srcTranId = srcTranId;
    }

    public String getSrcTranId() {
        return this.srcTranId;
    }

    public void setReqOrRes(String reqOrRes) {
        this.reqOrRes = reqOrRes;
    }

    public String getReqOrRes() {
        return this.reqOrRes;
    }

    public void setQueueMsgId(String queueMsgId) {
        this.queueMsgId = queueMsgId;
    }

    public String getQueueMsgId() {
        return this.queueMsgId;
    }

    public void setResQueue(Long resQueue) {
        this.resQueue = resQueue;
    }

    public Long getResQueue() {
        return this.resQueue;
    }

    public void setReqQueue(Long reqQueue) {
        this.reqQueue = reqQueue;
    }

    public Long getReqQueue() {
        return this.reqQueue;
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

    public void setCenterFwd2DstTime(Timestamp centerFwd2DstTime) {
        this.centerFwd2DstTime = centerFwd2DstTime;
    }

    public Timestamp getCenterFwd2DstTime() {
        return this.centerFwd2DstTime;
    }

    public void setDstReplyTime(Timestamp dstReplyTime) {
        this.dstReplyTime = dstReplyTime;
    }

    public Timestamp getDstReplyTime() {
        return this.dstReplyTime;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    public String getTabSuffix() {
        return tabSuffix;
    }

    public void setTabSuffix(String tabSuffix) {
        this.tabSuffix = tabSuffix;
    }
}
