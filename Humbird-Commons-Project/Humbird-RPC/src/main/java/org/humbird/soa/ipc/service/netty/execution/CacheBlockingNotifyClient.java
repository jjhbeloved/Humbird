package org.humbird.soa.ipc.service.netty.execution;

import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import org.humbird.soa.ipc.protoc.service.ClusterHService.BlockingNotify;
import org.humbird.soa.ipc.service.netty.ExecutableClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 15/6/9.
 */
public class CacheBlockingNotifyClient implements ExecutableClient {

    private static Logger log = LoggerFactory.getLogger(CacheBlockingNotifyClient.class);

    private Throwable error;

    @Override
    public void execute(RpcClientChannel channel) {
        try {
            long startTS = 0;
            long endTS = 0;
            startTS = System.currentTimeMillis();

            BlockingNotify.BlockingInterface notifyService = BlockingNotify.newBlockingStub(channel);
            final ClientRpcController controller = channel.newRpcController();
            controller.setTimeoutMs(2000);


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
