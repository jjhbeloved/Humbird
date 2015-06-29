package org.humbird.soa.ipc.avro.rpq;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.humbird.soa.ipc.avro.msg.HelloWorldImpl;
import org.humbird.soa.ipc.avro.msg.MessageProtocolImpl;
import org.humbird.soa.ipc.avro.vo.HelloWorld;
import org.humbird.soa.ipc.avro.vo.MessageProtocol;

import java.net.InetSocketAddress;

/**
 * Created by david on 15/6/6.
 */
public class HAvroServer {

    private Server server;

    private int port;

    public HAvroServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            server = new NettyServer(new SpecificResponder(MessageProtocol.class,
                    new MessageProtocolImpl()), new InetSocketAddress(port));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start2() {
        try {
            server = new NettyServer(new SpecificResponder(HelloWorld.class,
                    new HelloWorldImpl()), new InetSocketAddress(port));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        if (args.length != 1) {
//            System.out.println("Usage: Server port");
//            System.exit(0);
//        }
//        int port = Integer.parseInt(args[0]);
        int port = 39777;
        System.out.println("Starting server");
        new HAvroServer(port).start2();
//        new HAvroServer(port).start();
        System.out.println("Server started");
    }
}
