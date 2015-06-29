package org.humbird.soa.ipc.go;

import org.humbird.soa.ipc.protoc.service.HumbirdRpcClientAI;
import org.humbird.soa.ipc.service.netty.HumbirdRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by david on 15/6/11.
 */
public class HumbirdRPCClientMain {

    private static final Logger LOG = LoggerFactory.getLogger(HumbirdRPCClientMain.class);

    private static final String USAGE = "Usage: HumbirdRPCClientMain configfile";

    public static void main(String[] args) {
        HumbirdRPCClientMain humbirdRPCClientMain = new HumbirdRPCClientMain();
        try {
            humbirdRPCClientMain.initializeAndRun(args);
        } catch (IllegalArgumentException e) {
            LOG.error("Invalid arguments, exiting abnormally", e);
            LOG.info(USAGE);
            System.err.println(USAGE);
            System.exit(3);
        } catch (RpcConfig.ConfigException e) {
            LOG.error("Invalid config, exiting abnormally", e);
            System.err.println("Invalid config, exiting abnormally");
            System.exit(2);
        } catch (Exception e) {
            LOG.error("Unexpected exception, exiting abnormally", e);
            System.exit(1);
        }
    }

    protected void initializeAndRun(String[] args) throws IOException, RpcConfig.ConfigException {
        RpcConfig rpcConfig = new RpcConfig();
        rpcConfig.parse("/install_apps/test_bak/Humbird/Humbird-Commons-Project/Humbird-RPC/src/main/resources/HumbirdRpc2.cfg");

        HumbirdRpcClient humbirdRpcServer = new HumbirdRpcClientAI();
        humbirdRpcServer.run();

    }

}
