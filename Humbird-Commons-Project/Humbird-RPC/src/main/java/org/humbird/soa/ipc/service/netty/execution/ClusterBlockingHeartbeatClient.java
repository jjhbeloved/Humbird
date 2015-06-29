package org.humbird.soa.ipc.service.netty.execution;

import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import org.humbird.soa.ipc.go.RpcConfig;
import org.humbird.soa.ipc.protoc.service.ClusterHService;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeH.ProbeTypeH;
import org.humbird.soa.ipc.service.netty.ExecutableClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 15/6/9.
 */
public class ClusterBlockingHeartbeatClient implements ExecutableClient {

    private static Logger log = LoggerFactory.getLogger(CacheBlockingNotifyClient.class);

    private Throwable error;

    @Override
    public void execute(RpcClientChannel channel) {
        try {
            long startTS = 0;
            long endTS = 0;
            startTS = System.currentTimeMillis();

            ClusterHService.BlockingCheck.BlockingInterface anInterface = ClusterHService.BlockingCheck.newBlockingStub(channel);
            final ClientRpcController controller = channel.newRpcController();
            controller.setTimeoutMs(2000);

            ClusterSlaveH.ProbeH probeH = ClusterSlaveH.ProbeH.newBuilder()
                    .setIp("localhost")
                    .setServerName("test1")
                    .setVersion(RpcConfig.versions.get(ProbeTypeH.CACHE_FEED_BACK))
                    .setType(ClusterSlaveH.ProbeH.ProbeTypeH.HEARTBEAT)
                    .build();

            ClusterSlaveH.ProbeReplyH probeReplyH = anInterface.heartbeat(controller, probeH);

            if(probeReplyH.hasErrorCode()) {
                throw new ServiceException("Error code : " + probeReplyH.getErrorCode() + ", Error Message : " + (probeReplyH.hasErrorMessage() ? probeReplyH.getErrorMessage() : ""));
            }
            System.out.println(probeReplyH.getId());

            endTS = System.currentTimeMillis();
            log.info(" in " + (endTS - startTS)
                    / 1000 + "s");
        } catch ( Throwable t ) {
            this.error = t;
        }
    }

    @Override
    public Throwable getError() {
        return error;
    }
}
