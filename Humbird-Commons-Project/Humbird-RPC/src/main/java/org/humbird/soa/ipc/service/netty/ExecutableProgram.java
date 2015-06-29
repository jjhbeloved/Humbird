package org.humbird.soa.ipc.service.netty;

import com.googlecode.protobuf.pro.duplex.server.RpcClientRegistry;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeH.ProbeTypeH;

/**
 * Created by david on 15/6/9.
 */
public interface ExecutableProgram {

    public void execute(RpcClientRegistry registry, ProbeTypeH type) throws Throwable;

}
