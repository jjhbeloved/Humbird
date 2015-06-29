package org.humbird.soa.ipc.service.netty.wire;

/**
 * Created by david on 15/6/11.
 */
public class NettyServerVo {

    private boolean request = false;

    private boolean response = false;

    private boolean event = false;

    private int corePoolSize = 10;

    private int maximumPoolSize = 30;

    private long sleepTime = 10000;

    private long connectResponseTimeoutMillis =10000;

    private boolean compression =true;

    private HEventLoopGroup boss = new HEventLoopGroup(3, "boss", 1048576, 1048576, 10000);

    private HEventLoopGroup worker = new HEventLoopGroup(3, "worker", 1048576, 1048576, 10000);

    public boolean isRequest() {
        return request;
    }

    public void setRequest(boolean request) {
        this.request = request;
    }

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

    public boolean isEvent() {
        return event;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public long getConnectResponseTimeoutMillis() {
        return connectResponseTimeoutMillis;
    }

    public void setConnectResponseTimeoutMillis(long connectResponseTimeoutMillis) {
        this.connectResponseTimeoutMillis = connectResponseTimeoutMillis;
    }

    public boolean isCompression() {
        return compression;
    }

    public void setCompression(boolean compression) {
        this.compression = compression;
    }

    public HEventLoopGroup getBoss() {
        return boss;
    }

    public HEventLoopGroup getWorker() {
        return worker;
    }

    public class HEventLoopGroup<T> {
        private int threads;
        private String name;
        private T so_sndbuf;
        private T so_rcvbuf;
        private Integer connect_timeout_millis;

        public HEventLoopGroup(int threads, String name, T so_sndbuf, T so_rcvbuf, Integer connect_timeout_millis) {
            this.threads = threads;
            this.name = name;
            this.so_sndbuf = so_sndbuf;
            this.so_rcvbuf = so_rcvbuf;
            this.connect_timeout_millis = connect_timeout_millis;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public T getSo_sndbuf() {
            return so_sndbuf;
        }

        public void setSo_sndbuf(T so_sndbuf) {
            this.so_sndbuf = so_sndbuf;
        }

        public T getSo_rcvbuf() {
            return so_rcvbuf;
        }

        public void setSo_rcvbuf(T so_rcvbuf) {
            this.so_rcvbuf = so_rcvbuf;
        }

        public Integer getConnect_timeout_millis() {
            return connect_timeout_millis;
        }

        public void setConnect_timeout_millis(Integer connect_timeout_millis) {
            this.connect_timeout_millis = connect_timeout_millis;
        }
    }
}
