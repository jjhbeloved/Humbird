package org.humbird.soa.ipc.go;

import org.humbird.soa.ipc.go.RpcConfig.ConfigException;
import org.humbird.soa.ipc.protoc.service.HumbirdRpcServerAI;
import org.humbird.soa.ipc.service.netty.HumbirdRpcServer;
import org.humbird.soa.ipc.wire.CacheStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by david on 15/6/8.
 */
public class HumbirdRPCServerMain {

    private static final Logger LOG = LoggerFactory.getLogger(HumbirdRPCServerMain.class);

    private static final String USAGE = "Usage: HumbirdRPCServerMain configfile";

    public static void main(String[] args) {
        HumbirdRPCServerMain humbirdRPCServerMain = new HumbirdRPCServerMain();
        try {
            humbirdRPCServerMain.initializeAndRun(args);
        } catch (IllegalArgumentException e) {
            LOG.error("Invalid arguments, exiting abnormally", e);
            LOG.info(USAGE);
            System.err.println(USAGE);
            System.exit(3);
        } catch (ConfigException e) {
            LOG.error("Invalid config, exiting abnormally", e);
            System.err.println("Invalid config, exiting abnormally");
            System.exit(2);
        } catch (Exception e) {
            LOG.error("Unexpected exception, exiting abnormally", e);
            System.exit(1);
        }
    }

    protected void initializeAndRun(String[] args) throws IOException, ConfigException {
        RpcConfig rpcConfig = new RpcConfig();
        rpcConfig.parse("/install_apps/test_bak/Humbird/Humbird-Commons-Project/Humbird-RPC/src/main/resources/HumbirdRpc.cfg");

        Iterator<Map.Entry<String, CacheStat>> iterator =  RpcConfig.caches.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<String, CacheStat> entry = iterator.next();
            entry.getValue().setUpdated(false);
            System.out.println("key : " + entry.getKey() + ", val : " + entry.getValue().getLastDate() + ", count : " + CacheStat.needUpdateCount + ", has : " + CacheStat.updatedCount);
        }

        HumbirdRpcServer humbirdRpcServer = new HumbirdRpcServerAI();
        humbirdRpcServer.run();

    }
}
