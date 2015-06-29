package org.humbird.soa.ipc.service;

import org.humbird.soa.ipc.protoc.service.PClient;
import org.humbird.soa.ipc.service.netty.NettyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 15/6/9.
 */
public class RpcServiceFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(PClient.class);

    private static NettyService nettyService = null;

    public static NettyService createNettyService() {
        if(nettyService == null) {
            nettyService = new NettyService();
        }
        return nettyService;
    }
}
