package org.humbird.soa.ipc.service.netty.server;

import com.google.protobuf.RpcCallback;
import com.googlecode.protobuf.pro.duplex.LocalCallVariableHolder;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeReplyH;

/**
 * Created by david on 15/6/9.
 */
public class ProbeReplyCallback implements RpcCallback<ProbeReplyH> {

    public static final String KEY = "@";

    private LocalCallVariableHolder variableHolder;

    public ProbeReplyCallback(LocalCallVariableHolder variableHolder) {
        this.variableHolder = variableHolder;
    }

    @Override
    public void run(ProbeReplyH responseMessage) {
        variableHolder.storeCallLocalVariable(KEY, responseMessage);
    }

    public ProbeReplyH getProbeReplyH() {
        return (ProbeReplyH)variableHolder.getCallLocalVariable(KEY);
    }
}
