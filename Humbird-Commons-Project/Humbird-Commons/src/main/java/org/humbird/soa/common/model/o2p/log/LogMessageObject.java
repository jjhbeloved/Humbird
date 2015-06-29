package org.humbird.soa.common.model.o2p.log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by david on 15/1/1.
 */
public class LogMessageObject implements Serializable, Cloneable {

    private Map<String, String> javaFiledMap;

    private List contractInteractionList;

    private List endpointInteractionList;

    private List oriLogClobList;

    private List exceptionLogsList;

    private List ctgLogsList;

    private String orgId = "";

    private String busCode = "";

    private String svcCode = "";

    private int insertDBTimes = 0;

    private int number;

    private String srcSysSign;


    private String dataSourcekey;

    public String getDataSourcekey() {
        return dataSourcekey;
    }

    public void setDataSourcekey(String dataSourcekey) {
        this.dataSourcekey = dataSourcekey;
    }

    public LogMessageObject() {
        contractInteractionList = new ArrayList();
        endpointInteractionList = new ArrayList();
        oriLogClobList = new ArrayList();
        exceptionLogsList = new ArrayList();
        ctgLogsList = new ArrayList();
    }

    public Object clone() {
        try {
            LogMessageObject logMessageObject = new LogMessageObject();
            List contractInteractionCloneList = new ArrayList();
            for (int i = 0; i < contractInteractionList.size(); i++) {
                ContractInteraction contractInteractionSelf = (ContractInteraction) contractInteractionList.get(i);
                ContractInteraction contractInteractionClone = (ContractInteraction) contractInteractionSelf.clone();
                contractInteractionCloneList.add(contractInteractionClone);
            }
            logMessageObject.setContractInteractionList(contractInteractionCloneList);
            List endpointInteractionCloneList = new ArrayList();
            for (int i = 0; i < endpointInteractionList.size(); i++) {
                EndpointInteraction endpointInteractionSelf = (EndpointInteraction) endpointInteractionList.get(i);
                EndpointInteraction endpointInteractionClone = (EndpointInteraction) endpointInteractionSelf.clone();
                endpointInteractionCloneList.add(endpointInteractionClone);
            }
            logMessageObject.setEndpointInteractionList(endpointInteractionCloneList);
            List oriLogClobCloneList = new ArrayList();
            for (int i = 0; i < oriLogClobList.size(); i++) {
                OriLogClob oriLogClobSelf = (OriLogClob) oriLogClobList.get(i);
                OriLogClob OriLogClobClone = (OriLogClob) oriLogClobSelf.clone();
                oriLogClobCloneList.add(OriLogClobClone);
            }
            logMessageObject.setOriLogClobList(oriLogClobCloneList);
            List exceptionLogsCloneList = new ArrayList();
            for (int i = 0; i < exceptionLogsList.size(); i++) {
                ExceptionLogs exceptionLogsSelf = (ExceptionLogs) exceptionLogsList.get(i);
                ExceptionLogs exceptionLogsClone = (ExceptionLogs) exceptionLogsSelf.clone();
                exceptionLogsCloneList.add(exceptionLogsClone);
            }
            logMessageObject.setExceptionLogsList(exceptionLogsCloneList);
            List ctgLogsCloneList = new ArrayList();
            for (int i = 0; i < ctgLogsList.size(); i++) {
                CtgLogs ctgLogsSelf = (CtgLogs) ctgLogsList.get(i);
                CtgLogs ctgLogsClone = (CtgLogs) ctgLogsSelf.clone();
                ctgLogsCloneList.add(ctgLogsClone);
            }
            logMessageObject.setCtgLogsList(ctgLogsCloneList);

            logMessageObject.setBusCode(busCode);
            logMessageObject.setNumber(number);
            logMessageObject.setSrcSysSign(srcSysSign);
            logMessageObject.setSvcCode(svcCode);
            logMessageObject.setOrgId(orgId);
            logMessageObject.setInsertDBTimes(insertDBTimes);
            logMessageObject.setJavaFiledMap(javaFiledMap);
            return logMessageObject;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     * @param type
     * @param object
     * @param clearFlag
     */
    public void addLogMessage(String type, Object object, boolean clearFlag) {
        if (clearFlag == true) {
            if (object instanceof ContractInteraction) {
                contractInteractionList.clear();
            } else if (object instanceof EndpointInteraction) {
                endpointInteractionList.clear();
            } else if (object instanceof OriLogClob) {
                oriLogClobList.clear();
            } else if (object instanceof ExceptionLogs) {
                exceptionLogsList.clear();
            } else if (object instanceof CtgLogs) {
                ctgLogsList.clear();
            }
        }
        addLogMessage(type, object);
    }

    /**
     *
     * @param type
     * @param object
     */
    public void addLogMessage(String type, Object object) {
        if (object instanceof ContractInteraction) {
            contractInteractionList.add(object);
        } else if (object instanceof EndpointInteraction) {
            endpointInteractionList.add(object);
        } else if (object instanceof OriLogClob) {
            oriLogClobList.add(object);
        } else if (object instanceof ExceptionLogs) {
            exceptionLogsList.add(object);
        } else if (object instanceof CtgLogs) {
            ctgLogsList.add(object);
        }
    }

    /**
     * @param contractInteractionId
     */
    public void setAllContractInteractionId(String contractInteractionId) {
        for (int i = 0; i < contractInteractionList.size(); i++) {
            ContractInteraction iContractInteraction = (ContractInteraction) contractInteractionList.get(i);
            iContractInteraction.setContractInteractionId(contractInteractionId);
        }
        for (int i = 0; i < endpointInteractionList.size(); i++) {
            EndpointInteraction iEndpointInteraction = (EndpointInteraction) endpointInteractionList.get(i);
            iEndpointInteraction.setContractInteractionId(contractInteractionId);
        }
        for (int i = 0; i < oriLogClobList.size(); i++) {
            OriLogClob iOriLogClob = (OriLogClob) oriLogClobList.get(i);
            iOriLogClob.setContractInteractionId(contractInteractionId);
        }
        for (int i = 0; i < exceptionLogsList.size(); i++) {
            ExceptionLogs iExceptionLogs = (ExceptionLogs) exceptionLogsList.get(i);
            iExceptionLogs.setContractInteractionId(contractInteractionId);
        }
    }

    /**
     * @param tabSuffix
     */
    public void setDataBaseTableSuffix(String tabSuffix) {
        for (int i = 0; i < contractInteractionList.size(); i++) {
            ContractInteraction iContractInteraction = (ContractInteraction) contractInteractionList.get(i);
            iContractInteraction.setTabSuffix(tabSuffix);
        }
        for (int i = 0; i < endpointInteractionList.size(); i++) {
            EndpointInteraction iEndpointInteraction = (EndpointInteraction) endpointInteractionList.get(i);
            iEndpointInteraction.setTabSuffix(tabSuffix);
        }
        for (int i = 0; i < oriLogClobList.size(); i++) {
            OriLogClob iOriLogClob = (OriLogClob) oriLogClobList.get(i);
            iOriLogClob.setTabSuffix(tabSuffix);
        }
        for (int i = 0; i < exceptionLogsList.size(); i++) {
            ExceptionLogs iExceptionLogs = (ExceptionLogs) exceptionLogsList.get(i);
            iExceptionLogs.setTabSuffix(tabSuffix);
        }
        for (int i = 0; i < ctgLogsList.size(); i++) {
            CtgLogs ctgLogs = (CtgLogs) ctgLogsList.get(i);
            ctgLogs.setTabSuffix(tabSuffix);
        }
    }

    /**
     */
    public void initAllContractInteractionId() {
        setAllContractInteractionId("");
    }

    public List getContractInteractionList() {
        return contractInteractionList;
    }

    public void setContractInteractionList(List contractInteractionList) {
        this.contractInteractionList = contractInteractionList;
    }

    public List getEndpointInteractionList() {
        return endpointInteractionList;
    }

    public void setEndpointInteractionList(List endpointInteractionList) {
        this.endpointInteractionList = endpointInteractionList;
    }

    public List getOriLogClobList() {
        return oriLogClobList;
    }

    public void setOriLogClobList(List oriLogClobList) {
        this.oriLogClobList = oriLogClobList;
    }

    public List getExceptionLogsList() {
        return exceptionLogsList;
    }

    public void setExceptionLogsList(List exceptionLogsList) {
        this.exceptionLogsList = exceptionLogsList;
    }

    public List getCtgLogsList() {
        return ctgLogsList;
    }

    public void setCtgLogsList(List ctgLogsList) {
        this.ctgLogsList = ctgLogsList;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getBusCode() {
        return busCode;
    }

    public int getInsertDBTimes() {
        return insertDBTimes;
    }

    public void setInsertDBTimes(int insertDBTimes) {
        this.insertDBTimes = insertDBTimes;
    }

    public void setBusCode(String busCode) {
        this.busCode = busCode;
    }

    public String getSvcCode() {
        return svcCode;
    }

    public void setSvcCode(String svcCode) {
        this.svcCode = svcCode;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getSrcSysSign() {
        return srcSysSign;
    }

    public void setSrcSysSign(String srcSysSign) {
        this.srcSysSign = srcSysSign;
    }

    public Map<String, String> getJavaFiledMap() {
        return javaFiledMap;
    }

    public void setJavaFiledMap(Map<String, String> javaFiledMap) {
        this.javaFiledMap = javaFiledMap;
    }
}