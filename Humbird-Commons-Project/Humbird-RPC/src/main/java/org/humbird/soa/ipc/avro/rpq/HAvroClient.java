package org.humbird.soa.ipc.avro.rpq;

import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.humbird.soa.ipc.avro.vo.Greeting;
import org.humbird.soa.ipc.avro.vo.HelloWorld;
import org.humbird.soa.ipc.avro.vo.Message;
import org.humbird.soa.ipc.avro.vo.MessageProtocol;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by david on 15/6/6.
 */
public class HAvroClient {

    private String host;

    private int port = 0;

    private int size = 0;

    private int count = 0;

    public HAvroClient(String host, int port, int size, int count) {
        this.host = host;
        this.port = port;
        this.size = size;
        this.count = count;
    }

    public long sendMessage() throws Exception {
        NettyTransceiver client = new NettyTransceiver(new InetSocketAddress(host, port));
        MessageProtocol proxy = SpecificRequestor.getClient(
                MessageProtocol.class, client);
        Message message = Message.newBuilder()
                .setName("test")
                .setType(1)
                .setPrice(999.99)
                .setValid(true)
                .setContent(ByteBuffer.wrap("Hello".getBytes()))
                .setTags(new ArrayList<CharSequence>(){
                    {
                        add("test");
                        add("hello");
                        add("world");
                    }
                })
                .build();
        long start = System.currentTimeMillis();
        System.out.println(message);
        for (int i = 0; i < count; i++) {
            proxy.sendMessage(message);
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start) + " ms");
        return end - start;
    }

    public long sendMessage2() throws Exception {
        NettyTransceiver client = new NettyTransceiver(new InetSocketAddress(host, port));
        HelloWorld proxy = SpecificRequestor.getClient(
                HelloWorld.class, client);
        Greeting greeting = new Greeting("how are you");
        greeting.setMessage("ggg");

        long start = System.currentTimeMillis();
        proxy.hello(greeting);
        long end = System.currentTimeMillis();
        System.out.println((end - start) + " ms");
        return end - start;
    }

    public long run() {
        long res = 0;
        try {
            res = sendMessage2();
//            res = sendMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
//        if (args.length != 4) {
//            System.out.println("Usage: Client host port dataSize count");
//            System.exit(0);
//        }
        String host = "localhost";
        int port = 39777;
        int size = 1;
        int count = 1;
        new HAvroClient(host, port, size, count).run();
    }
}
