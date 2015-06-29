package org.humbird.soa.ipc.service;

/**
 * Created by david on 15/6/9.
 */
public interface NIOService {

    void createServerPipelineFactory();

    void createClientPipelineFactory();

    void registerConnectionEventListener();

    void createLogger();

    void CallExecutor(int corePoolSize, int maximumPoolSize);
}
