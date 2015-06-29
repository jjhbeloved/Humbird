package org.humbird.soa.ipc.protoc.service;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.*;
import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientPipelineFactory;
import com.googlecode.protobuf.pro.duplex.client.RpcClientConnectionWatchdog;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.CategoryPerServiceLogger;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.humbird.soa.ipc.protoc.vo.LeeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

/**
 * Created by david on 15/6/7.
 */
public class PClient {
    private static Logger log = LoggerFactory.getLogger(PClient.class);

    private static RpcClientChannel channel = null;

    public static void main(String[] args) throws Exception {
//        if (args.length != 4) {
//            System.err
//                    .println("usage: <serverHostname> <serverPort> <clientHostname> <clientPort>");
//            System.exit(-1);
//        }
//        String serverHostname = args[0];
//        int serverPort = Integer.parseInt(args[1]);
//        String clientHostname = args[2];
//        int clientPort = Integer.parseInt(args[3]);

        String serverHostname = "localhost";
        int serverPort = 2717;
        String clientHostname = "localhost";
        int clientPort = 2718;

        PeerInfo client = new PeerInfo(clientHostname, clientPort);
        PeerInfo server = new PeerInfo(serverHostname, serverPort);

        try {
            DuplexTcpClientPipelineFactory clientFactory = new DuplexTcpClientPipelineFactory();
            // force the use of a local port
            // - normally you don't need this
            clientFactory.setClientInfo(client);

            ExtensionRegistry r = ExtensionRegistry.newInstance();
            LeeInfo.registerAllExtensions(r);
//            PingPong.registerAllExtensions(r);
            clientFactory.setExtensionRegistry(r);

            clientFactory.setConnectResponseTimeoutMillis(10000);
            RpcServerCallExecutor rpcExecutor = new ThreadPoolCallExecutor(3, 10);
            clientFactory.setRpcServerCallExecutor(rpcExecutor);
            // open compress
            clientFactory.setCompression(true);

            // RPC payloads are uncompressed when logged - so reduce logging
            // 关闭 减少日志 或者com.googlecode.protobuf.pro.duplex.logging.nulllogger可以代替的，将不记录任何categoryperservicelogger。
            CategoryPerServiceLogger logger = new CategoryPerServiceLogger();
            logger.setLogRequestProto(false);
            logger.setLogResponseProto(false);
            logger.setLogEventProto(false);
            clientFactory.setRpcLogger(logger);

            // hook
            final RpcCallback<LeeInfo.LeeReply> clientResponseCallback = new RpcCallback<LeeInfo.LeeReply>() {
                @Override
                public void run(LeeInfo.LeeReply reply) {
                    log.info("+++++ ---- +++++" + reply);
                }
            };

            // Set up the event pipeline factory.
            // setup a RPC event listener - it just logs what happens
            RpcConnectionEventNotifier rpcEventNotifier = new RpcConnectionEventNotifier();

            final RpcConnectionEventListener listener = new RpcConnectionEventListener() {

                @Override
                public void connectionReestablished(RpcClientChannel clientChannel) {
                    log.info("------- connectionReestablished " + clientChannel);
                    channel = clientChannel;
                }

                @Override
                public void connectionOpened(RpcClientChannel clientChannel) {
                    log.info("------- connectionOpened " + clientChannel);
                    channel = clientChannel;
                    clientChannel.setOobMessageCallback(LeeInfo.LeeReply.getDefaultInstance(), clientResponseCallback);
                }

                @Override
                public void connectionLost(RpcClientChannel clientChannel) {
                    log.info("------- connectionLost " + clientChannel);
                }

                @Override
                public void connectionChanged(RpcClientChannel clientChannel) {
                    log.info("------- connectionChanged " + clientChannel);
                    channel = clientChannel;
                }
            };
            rpcEventNotifier.addEventListener(listener);
            clientFactory.registerConnectionEventListener(rpcEventNotifier);

            Bootstrap bootstrap = new Bootstrap();
            EventLoopGroup workers = new NioEventLoopGroup(16,new RenamingThreadFactoryProxy("workers", Executors.defaultThreadFactory()));

            bootstrap.group(workers);
            bootstrap.handler(clientFactory);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,10000);
            bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
            bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);

            RpcClientConnectionWatchdog watchdog = new RpcClientConnectionWatchdog(clientFactory,bootstrap);
            rpcEventNotifier.addEventListener(watchdog);
            watchdog.start();

            CleanShutdownHandler shutdownHandler = new CleanShutdownHandler();
            shutdownHandler.addResource(workers);
            shutdownHandler.addResource(rpcExecutor);

            clientFactory.peerWith(server, bootstrap);

            while (true && channel != null) {

                UserService.GetUserService.BlockingInterface userService = UserService.GetUserService.newBlockingStub(channel);
                final ClientRpcController controller = channel.newRpcController();
                controller.setTimeoutMs(0);

                LeeInfo.LeeRequest.Builder builder = LeeInfo.LeeRequest.newBuilder();
                builder.setVersion("77");
                LeeInfo.LeeRequest leeRequest = builder.build();

                try {
                    LeeInfo.LeeReply leeReply = userService.getConfig(controller, leeRequest);
                    System.out.println("----" + leeReply.getBinaryShareIp());
                    System.out.println("----" + leeReply.getHttpServerIp());
                    System.out.println("----" + leeReply.getPlayerType());
                    System.out.println("----" + leeReply.getTcIf());
                    System.out.println("----" + leeReply.getTcIp());
                } catch ( ServiceException e ) {
                    log.warn("Call failed.", e);
                }

//                LeeInfo.LeeReply leeReply = LeeInfo.LeeReply.newBuilder().setHttpServerIp("+.+.+.+").build();
//                ChannelFuture oobSend = channel.sendOobMessage(leeReply);
//                if (!oobSend.isDone()) {
//                    log.info("Waiting for completion.");
//                    oobSend.syncUninterruptibly();
//                }
//                if (!oobSend.isSuccess()) {
//                    log.warn("OobMessage send failed." + oobSend.cause());
//                }
                Thread.sleep(10000);

            }

        } catch ( Exception e ) {
            log.warn("Failure.", e);
        } finally {
            System.exit(0);
        }
    }
}
