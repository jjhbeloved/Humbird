package org.humbird.soa.ipc.service.netty.execution;

import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import org.humbird.soa.ipc.go.RpcConfig;
import org.humbird.soa.ipc.protoc.service.ClusterHService.BlockingFeedBack;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeReplyH;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeH;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeH.ProbeTypeH;
import org.humbird.soa.ipc.service.netty.ExecutableClient;
import org.humbird.soa.ipc.service.netty.server.ProbeReplyCallback;
import org.humbird.soa.ipc.wire.CacheStat;
import org.humbird.soa.ipc.wire.ReplyStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 15/6/9.
 */
public class CacheBlockingFeedbackClient implements ExecutableClient {

    private static Logger log = LoggerFactory.getLogger(CacheBlockingFeedbackClient.class);

    private Throwable error;

    @Override
    public void execute(RpcClientChannel channel) {
        try {
            long startTS = 0;
            long endTS = 0;
            startTS = System.currentTimeMillis();
            BlockingFeedBack.BlockingInterface feedbackService = BlockingFeedBack.newBlockingStub(channel);
            final ClientRpcController controller = channel.newRpcController();
            controller.setTimeoutMs(2000);
            ProbeReplyCallback prc = new ProbeReplyCallback(controller);
            controller.setOobResponseCallback(ProbeReplyH.getDefaultInstance(), prc);

            ProbeH.Builder probeHBuilider = ProbeH.newBuilder()
                    .setId(RpcConfig.caches.get(RpcConfig.local.getServerName()).getId())
                    .setServerName(RpcConfig.local.getServerName())
                    .setVersion(RpcConfig.versions.get(ProbeTypeH.CACHE_FEED_BACK));
            ProbeReplyH probeReplyH = feedbackService.feedback(controller, probeHBuilider.build());
            if(probeReplyH.hasErrorCode()) {
                throw new ServiceException("Error code : " + probeReplyH.getErrorCode() + ", Error Message : " + (probeReplyH.hasErrorMessage() ? probeReplyH.getErrorMessage() : ""));
            }

            invoke(probeReplyH);

//            ProbeReplyH probeReplyH2 = prc.getProbeReplyH();

            endTS = System.currentTimeMillis();
        } catch ( Throwable t ) {
            log.error(t.getMessage());
            this.error = t;
        }
    }

    @Override
    public Throwable getError() {
        return error;
    }

    private void invoke(ProbeReplyH probeReplyH) {
        CacheStat cacheStat = RpcConfig.caches.get(RpcConfig.local.getServerName());
        if(probeReplyH.getId() == ReplyStat.NONE) {
            System.out.println("####### none");
            // ... none
        } else if(probeReplyH.getId() == ReplyStat.UPDATE) {
            System.out.println("####### update");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // done...
            RpcConfig.versions.put(ProbeTypeH.CACHE_FEED_BACK, probeReplyH.getVersion());
            cacheStat.setId(probeReplyH.getId());
        } else if(probeReplyH.getId() == ReplyStat.SWITCH) {
            // ... switch
            System.out.println("####### donw switch");
            cacheStat.setId(probeReplyH.getId());
        } else if(probeReplyH.getId() == ReplyStat.WAIT_SWITCH) {
            System.out.println("####### wait switch");
            cacheStat.setId(probeReplyH.getId());
        }
    }
}
