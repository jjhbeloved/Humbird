package org.humbird.soa.ipc.service.netty.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.execute.ServerRpcController;
import org.humbird.soa.ipc.go.RpcConfig;
import org.humbird.soa.ipc.protoc.service.ClusterHService.BlockingCheck;
import org.humbird.soa.ipc.protoc.service.ClusterHService.BlockingFeedBack;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeH;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeH.ProbeTypeH;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeReplyH;
import org.humbird.soa.ipc.wire.CacheStat;
import org.humbird.soa.ipc.wire.ReplyStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 15/6/10.
 */
public class CacheServiceFactory {

    private static Logger log = LoggerFactory.getLogger(CacheServiceFactory.class);

    public static void cacheFeedBack(RpcController controller, ProbeH request) {

        ServerRpcController rpcController = ServerRpcController.getRpcController(controller);

        System.out.println("########################");
        System.out.println("####" + request.getProbeMessage() + "###");
        System.out.println("####" + request.getIp() + "###");
        System.out.println("####" + request.getServerName() + "###");
        System.out.println("########################");
        ProbeReplyH probeReplyH = ProbeReplyH.newBuilder()
                .setId(1)
                .setResponseMessage(ByteString.copyFromUtf8("That's Perfect"))
                .build();
        rpcController.sendOobResponse(probeReplyH);
    }

    public static class BlockingFeedBackServer implements BlockingFeedBack.BlockingInterface {

        @Override
        public ProbeReplyH feedback(RpcController controller, ProbeH request) throws ServiceException {
            int retId = ReplyStat.NONE;
            int id = request.getId();
            int version = request.getVersion();
            ProbeReplyH.Builder builder = ProbeReplyH.newBuilder();
            int localVersion = RpcConfig.versions.get(ProbeTypeH.CACHE_FEED_BACK);
            if(localVersion == version) {
                if(id == ReplyStat.NONE) {
                    retId = ReplyStat.NONE;
                } else if(id == ReplyStat.UPDATE) {
                    log.info("######## switch " + request.getServerName());
                    CacheStat cacheStat = RpcConfig.caches.get(request.getServerName());
                    if(!cacheStat.isUpdated()) {
                        cacheStat.setUpdated(true);
                    }
                    if(CacheStat.updatedCount.get() == 0) {
                        retId = ReplyStat.SWITCH;
                    } else {
                        retId = ReplyStat.WAIT_SWITCH;
                    }
                } else if(id == ReplyStat.WAIT_SWITCH) {
                    log.info("######## WAIT_SWITCH : " + request.getServerName());
                    log.info("######## WAIT_SWITCH : " + CacheStat.updatedCount.get());
                    log.info("######## ALL COUNT : " + CacheStat.needUpdateCount.get());
                    if(CacheStat.updatedCount.get() == 0) {
                        retId = ReplyStat.SWITCH;
                    } else {
                        retId = ReplyStat.WAIT_SWITCH;
                    }
                }
            } else {
                log.info("######## update");
                retId = ReplyStat.UPDATE;
            }
            return builder.setId(retId).build();
        }
    }

    public static class BlockingCheckServer implements BlockingCheck.BlockingInterface {

        @Override
        public ProbeReplyH heartbeat(RpcController controller, ProbeH request) throws ServiceException {
            return ProbeReplyH.newBuilder().setId(0).build();
        }
    }

}
