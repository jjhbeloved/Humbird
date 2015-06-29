package org.humbird.soa.ipc.service.netty;

import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import org.humbird.soa.ipc.service.netty.ExecutableClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by david on 15/6/9.
 */
public class ClientExecutor {

    private static Logger log = LoggerFactory.getLogger(ClientExecutor.class);

    private transient static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public ClientExecutor() {
    }

    public void execute(ExecutableClient client, RpcClientChannel channel) {
        execute(new ExecutableClient[]{client}, channel);
    }

    public void execute(ExecutableClient[] clients, RpcClientChannel channel) {
        HClientThread[] threads = new HClientThread[clients.length];
        for( int i = 0; i < threads.length; i++ ) {
            HClientThread c = new HClientThread(clients[i], channel);
            threads[i] = c;
            cachedThreadPool.execute(c);
        }
//        for( int i = 0; i < threads.length; i++ ) {
//            if (threads[i].getError() !=null) {
//                System.out.println();
//                throw threads[i].getError();
//            }
//        }

    }

    public void shutdown() {
        cachedThreadPool.shutdown();
    }


    private static class HClientThread extends Thread {

        ExecutableClient client;
        RpcClientChannel channel;

        public HClientThread( ExecutableClient client, RpcClientChannel channel ) {
            this.client = client;
            this.channel = channel;
        }

        @Override
        public void run() {
            client.execute(channel);
        }

        public Throwable getError() {
            return this.client.getError();
        }
    }
}
