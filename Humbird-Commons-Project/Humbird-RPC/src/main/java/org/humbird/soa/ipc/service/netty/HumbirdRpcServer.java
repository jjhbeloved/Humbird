package org.humbird.soa.ipc.service.netty;

import com.google.protobuf.ExtensionRegistry;
import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.CategoryPerServiceLogger;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.humbird.soa.ipc.go.RpcConfig;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH;
import org.humbird.soa.ipc.service.RpcServiceFactory;
import org.humbird.soa.ipc.service.netty.wire.NettyServerVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 15/6/10.
 */
public abstract class HumbirdRpcServer {

    private static Logger log = LoggerFactory.getLogger(HumbirdRpcServer.class);

    private NettyServerVo nettyServerVo = new NettyServerVo();

    public void run() {
        before(nettyServerVo);
        String serverHostname = RpcConfig.masterMember.getIp();
        int serverPort = RpcConfig.masterMember.getPort();

        NettyService nettyService = RpcServiceFactory.createNettyService();

        PeerInfo serverInfo = nettyService.createPeerInfo(serverHostname, serverPort);

        // RPC payloads are uncompressed when logged - so reduce logging
        CategoryPerServiceLogger logger = nettyService.createLogger(nettyServerVo.isRequest(), nettyServerVo.isResponse(), nettyServerVo.isEvent());
//        NullLogger logger = new NullLogger();

        // Configure the server.
        DuplexTcpServerPipelineFactory serverFactory = nettyService.createDuplexTcpServerPipelineFactory(serverInfo);

        ExtensionRegistry r = ExtensionRegistry.newInstance();
        ClusterSlaveH.registerAllExtensions(r);
        serverFactory.setExtensionRegistry(r);

        RpcServerCallExecutor rpcExecutor = new ThreadPoolCallExecutor(nettyServerVo.getCorePoolSize(), nettyServerVo.getMaximumPoolSize());
        serverFactory.setRpcServerCallExecutor(rpcExecutor);
        serverFactory.setLogger(logger);

        // setup a RPC event listener - it just logs what happens
        RpcConnectionEventListener listener = new RpcConnectionEventListener() {

            @Override
            public void connectionReestablished(RpcClientChannel clientChannel) {
                log.info("------- connectionReestablished " + clientChannel);
            }

            @Override
            public void connectionOpened(RpcClientChannel clientChannel) {
                log.info("------- connectionOpened " + clientChannel);
            }

            @Override
            public void connectionLost(RpcClientChannel clientChannel) {
                log.info("------- connectionLost " + clientChannel);
            }

            @Override
            public void connectionChanged(RpcClientChannel clientChannel) {
                log.info("------- connectionChanged " + clientChannel);
            }
        };
        RpcConnectionEventNotifier rpcEventNotifier = nettyService.createRpcConnectionEventNotifier(listener);
        serverFactory.registerConnectionEventListener(rpcEventNotifier);

        register(serverFactory);

        // init netty
        ServerBootstrap bootstrap = nettyService.createServerBootstrap();

        EventLoopGroup boss = nettyService.createEventLoopGroup(nettyServerVo.getBoss().getThreads(), nettyServerVo.getBoss().getName());
        EventLoopGroup workers = nettyService.createEventLoopGroup(nettyServerVo.getWorker().getThreads(), nettyServerVo.getWorker().getName());
        bootstrap.group(boss, workers);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_SNDBUF, (Integer) nettyServerVo.getBoss().getSo_sndbuf());
        bootstrap.option(ChannelOption.SO_RCVBUF, (Integer) nettyServerVo.getBoss().getSo_rcvbuf());
        bootstrap.childOption(ChannelOption.SO_RCVBUF, (Integer) nettyServerVo.getWorker().getSo_rcvbuf());
        bootstrap.childOption(ChannelOption.SO_SNDBUF, (Integer) nettyServerVo.getWorker().getSo_sndbuf());
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.childHandler(serverFactory);
        bootstrap.localAddress(serverInfo.getPort());

        // shutdown release source
        CleanShutdownHandler shutdownHandler = nettyService.createCleanShutdownHandler();
        shutdownHandler.addResource(boss);
        shutdownHandler.addResource(workers);
        shutdownHandler.addResource(rpcExecutor);

        ChannelFuture future = null;
        // Bind and start to accept incoming connections.
        try {
            future = bootstrap.bind().sync();
            log.info("Serving " + bootstrap);

            while (true) {
                log.info("Sleeping 10s before retesting clients.");
                Thread.sleep(100000);
//                new CacheService().execute(serverFactory.getRpcClientRegistry(), null);
            }

        } catch (Throwable e) {
            log.error("Throwable.", e);
        } finally {
            if(future != null) {
                try {
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    log.error("Throwable.", e);
                }
            }
        }

    }

    protected abstract void before(NettyServerVo nettyServerVo );

    protected abstract void register(DuplexTcpServerPipelineFactory factory);

    protected abstract void after(NettyServerVo nettyServerVo );
}
