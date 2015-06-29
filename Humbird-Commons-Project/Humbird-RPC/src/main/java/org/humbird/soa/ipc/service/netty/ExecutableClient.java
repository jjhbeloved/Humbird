package org.humbird.soa.ipc.service.netty;

import com.googlecode.protobuf.pro.duplex.RpcClientChannel;

/**
 * Created by david on 15/6/9.
 */
public interface ExecutableClient {

    public void execute(RpcClientChannel channel);

    public Throwable getError();
}
