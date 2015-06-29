package org.humbird.soa.ipc.protoc.service;

import com.google.protobuf.BlockingService;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;
import org.humbird.soa.ipc.service.netty.HumbirdRpcServer;
import org.humbird.soa.ipc.service.netty.server.CacheServiceFactory;
import org.humbird.soa.ipc.service.netty.wire.NettyServerVo;

/**
 * Created by david on 15/6/11.
 */
public class HumbirdRpcServerAI extends HumbirdRpcServer {

    @Override
    protected void before(NettyServerVo nettyServerVo) {

    }

    @Override
    protected void register(DuplexTcpServerPipelineFactory factory) {
        // we give the server a blocking and non blocking (pong capable) Ping Service
        // use registry service
        BlockingService bFeedBackService =  ClusterHService.BlockingFeedBack.newReflectiveBlockingService(new CacheServiceFactory.BlockingFeedBackServer());
//        BlockingService bFeedBackService =  ClusterHService.BlockingCheck.newReflectiveBlockingService(new CacheServiceFactory.BlockingCheckServer());
        factory.getRpcServiceRegistry().registerService(true, bFeedBackService);
    }

    @Override
    protected void after(NettyServerVo nettyServerVo) {

    }

}
