package org.humbird.soa.ipc.protoc.service;

import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientPipelineFactory;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeH.ProbeTypeH;
import org.humbird.soa.ipc.service.netty.CacheService;
import org.humbird.soa.ipc.service.netty.HumbirdRpcClient;
import org.humbird.soa.ipc.service.netty.wire.NettyServerVo;

/**
 * Created by david on 15/6/12.
 */
public class HumbirdRpcClientAI extends HumbirdRpcClient {

    private static CacheService cacheService = new CacheService();

    @Override
    protected void before(NettyServerVo nettyServerVo) {

    }

    @Override
    protected void register(DuplexTcpClientPipelineFactory factory) throws Throwable {
        cacheService.execute(factory.getRpcClientRegistry(), ProbeTypeH.CACHE_FEED_BACK);
    }

    @Override
    protected void after(NettyServerVo nettyServerVo) {

    }

}
