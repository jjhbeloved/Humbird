package org.humbird.soa.ipc.service.netty;

import com.google.protobuf.ExtensionRegistry;
import com.googlecode.protobuf.pro.duplex.*;
import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientPipelineFactory;
import com.googlecode.protobuf.pro.duplex.client.RpcClientConnectionWatchdog;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.CategoryPerServiceLogger;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutExecutor;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutExecutor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.humbird.soa.ipc.go.RpcConfig;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH;
import org.humbird.soa.ipc.service.RpcServiceFactory;
import org.humbird.soa.ipc.service.netty.wire.NettyServerVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by david on 15/6/10.
 */
public abstract class HumbirdRpcClient {

    private static Logger log = LoggerFactory.getLogger(HumbirdRpcServer.class);

    private NettyServerVo nettyServerVo = new NettyServerVo();

    public void run() {
        before(nettyServerVo);
        String serverHostname = RpcConfig.masterMember.getIp();
        int serverPort = RpcConfig.masterMember.getPort();
        String clientHostname = RpcConfig.local.getIp();
        int clientPort = RpcConfig.local.getPort();

        NettyService nettyService = RpcServiceFactory.createNettyService();

        PeerInfo client = nettyService.createPeerInfo(clientHostname, clientPort);
        PeerInfo server = nettyService.createPeerInfo(serverHostname, serverPort);

        DuplexTcpClientPipelineFactory clientFactory = nettyService.createDuplexTcpClientPipelineFactory();
        clientFactory.setClientInfo(client);
        // RPC payloads are uncompressed when logged - so reduce logging
        CategoryPerServiceLogger logger = nettyService.createLogger(nettyServerVo.isRequest(), nettyServerVo.isResponse(), nettyServerVo.isEvent());

        ExtensionRegistry r = ExtensionRegistry.newInstance();
        ClusterSlaveH.registerAllExtensions(r);
        clientFactory.setExtensionRegistry(r);

        clientFactory.setConnectResponseTimeoutMillis(nettyServerVo.getConnectResponseTimeoutMillis());
        RpcServerCallExecutor rpcExecutor = new ThreadPoolCallExecutor(nettyServerVo.getCorePoolSize(), nettyServerVo.getMaximumPoolSize());
        clientFactory.setRpcServerCallExecutor(rpcExecutor);
        // open compress
        clientFactory.setCompression(nettyServerVo.isCompression());
        clientFactory.setRpcLogger(logger);

        final RpcConnectionEventListener listener = new RpcConnectionEventListener() {

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
        clientFactory.registerConnectionEventListener(rpcEventNotifier);

        Bootstrap bootstrap = nettyService.createBootstrap();

        EventLoopGroup workers = nettyService.createEventLoopGroup(nettyServerVo.getWorker().getThreads(), nettyServerVo.getWorker().getName());

        bootstrap.group(workers);
        bootstrap.handler(clientFactory);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, nettyServerVo.getWorker().getConnect_timeout_millis());
        bootstrap.option(ChannelOption.SO_SNDBUF, (Integer) nettyServerVo.getWorker().getSo_sndbuf());
        bootstrap.option(ChannelOption.SO_RCVBUF, (Integer) nettyServerVo.getWorker().getSo_rcvbuf());

        RpcClientConnectionWatchdog watchdog = new RpcClientConnectionWatchdog(clientFactory,bootstrap);
        rpcEventNotifier.addEventListener(watchdog);
        watchdog.start();

        RpcTimeoutExecutor timeoutExecutor = new TimeoutExecutor(1,5);
        RpcTimeoutChecker checker = new TimeoutChecker();
        checker.setTimeoutExecutor(timeoutExecutor);
        checker.startChecking(clientFactory.getRpcClientRegistry());

        // shutdown release source
        CleanShutdownHandler shutdownHandler = nettyService.createCleanShutdownHandler();
        shutdownHandler.addResource(workers);
        shutdownHandler.addResource(rpcExecutor);
        shutdownHandler.addResource(checker);
        shutdownHandler.addResource(bootstrap.group());

        for(int i=0; i<100; i++) {
            try {
                clientFactory.peerWith(server, bootstrap);
                break;
            } catch (IOException e) {
                log.error(e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        try {
            while( true ) {
                register(clientFactory);
                Thread.sleep(10000);
            }
        } catch (Throwable e) {
            log.error("Throwable.", e);
        } finally {
            System.exit(0);
        }
    }

    protected abstract void before(NettyServerVo nettyServerVo );

    protected abstract void register(DuplexTcpClientPipelineFactory factory) throws Throwable;

    protected abstract void after(NettyServerVo nettyServerVo );
}
