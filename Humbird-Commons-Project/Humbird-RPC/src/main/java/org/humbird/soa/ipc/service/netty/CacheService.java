package org.humbird.soa.ipc.service.netty;

import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.server.RpcClientRegistry;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeH.ProbeTypeH;
import org.humbird.soa.ipc.service.netty.execution.CacheBlockingFeedbackClient;
import org.humbird.soa.ipc.service.netty.execution.CacheBlockingNotifyClient;
import org.humbird.soa.ipc.service.netty.execution.ClusterBlockingHeartbeatClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by david on 15/6/9.
 */
public class CacheService implements ExecutableProgram {

    private static Logger log = LoggerFactory.getLogger(CacheService.class);

    public CacheService() {

    }

    @Override
    public void execute(RpcClientRegistry registry, ProbeTypeH type) {
        List<RpcClientChannel> channels = registry.getAllClients();
        if ( channels.size() <= 0) {
            log.info("No clients currently connected.");
        }
        for( RpcClientChannel channel : channels ) {
            doReverseTests(channel, type);
        }
    }

    protected void doReverseTests(RpcClientChannel channel, ProbeTypeH type) {
        ExecutableClient c = null;
        ClientExecutor exec = new ClientExecutor();
        if(ProbeTypeH.CACHE_FEED_BACK.equals(type)) {
            c = new CacheBlockingFeedbackClient();
        } else if(ProbeTypeH.CACHE_NOTIFY.equals(type)) {
            c = new CacheBlockingNotifyClient();
        } else if(ProbeTypeH.HEARTBEAT.equals(type)) {
            c = new ClusterBlockingHeartbeatClient();
        }

        exec.execute(c, channel);
    }

}
