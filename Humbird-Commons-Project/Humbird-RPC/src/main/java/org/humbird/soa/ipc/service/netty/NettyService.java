package org.humbird.soa.ipc.service.netty;

import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientPipelineFactory;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.CategoryPerServiceLogger;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.Executors;

/**
 * Created by david on 15/6/9.
 */
public class NettyService {

    //  *********
    //  1.
    //  create client or server PeerInfo
    //  *********
    public PeerInfo createPeerInfo(String hostName, int port) {
        return new PeerInfo(hostName, port);
    }

    public PeerInfo createPeerInfo(String hostName, int port, String pid) {
        return new PeerInfo(hostName, port, pid);
    }

    //  *********
    //  2.
    //  create logger
    //  if you want to oobmessage.info, pls add below data in logback.xml

    //  <logger name="oobmessage.info" level="ERROR" additivity="false">
    //      <appender-ref ref="STDOUT"/>
    //  </logger>
    //  *********
    public CategoryPerServiceLogger createLogger(boolean request, boolean response, boolean event) {
        CategoryPerServiceLogger logger = new CategoryPerServiceLogger();
        logger.setLogRequestProto(request);
        logger.setLogResponseProto(response);
        logger.setLogEventProto(event);
        return logger;
    }

    //  *********
    //  4.
    //  create DuplexTcp Server/Client PipelineFactory
    //  *********
    public DuplexTcpServerPipelineFactory createDuplexTcpServerPipelineFactory(PeerInfo peerInfo) {
        return new DuplexTcpServerPipelineFactory(peerInfo);
    }

    public DuplexTcpClientPipelineFactory createDuplexTcpClientPipelineFactory() {
        return new DuplexTcpClientPipelineFactory();
    }

    //  *********
    //  5.
    //  create RpcConnectionEventNotifier
    //  *********
    public RpcConnectionEventNotifier createRpcConnectionEventNotifier() {
        return new RpcConnectionEventNotifier();
    }

    public RpcConnectionEventNotifier createRpcConnectionEventNotifier(RpcConnectionEventListener rpcConnectionEventListener) {
        RpcConnectionEventNotifier rpcConnectionEventNotifier = new RpcConnectionEventNotifier();
        rpcConnectionEventNotifier.setEventListener(rpcConnectionEventListener);
        return rpcConnectionEventNotifier;
    }

    //  *********
    //  6.
    //  create EventLoopGroup
    //  *********
    public EventLoopGroup createEventLoopGroup(int nThreads, String namePrefix) {
        return new NioEventLoopGroup(nThreads, new RenamingThreadFactoryProxy(namePrefix, Executors.defaultThreadFactory()));
    }

    //  *********
    //  7.
    //  create client/server BootStrap
    //  *********
    public Bootstrap createBootstrap() {
        return new Bootstrap();
    }

    public ServerBootstrap createServerBootstrap() {
        return new ServerBootstrap();
    }

    //  *********
    //  8.
    //  create CleanShutdownHandler
    //  *********
    public CleanShutdownHandler createCleanShutdownHandler() {
        return new CleanShutdownHandler();
    }


}
