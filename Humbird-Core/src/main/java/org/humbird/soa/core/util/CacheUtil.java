package org.humbird.soa.core.util;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import net.rubyeye.xmemcached.XMemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.humbird.soa.core.cache.CacheContext;
import org.humbird.soa.core.cache.JAVACacheWrapper;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by david on 15/4/4.
 */
public class CacheUtil {

    private static WebApplicationContext ctx = null;

    public final static String JVM_CAPACITY = "org.humbird.soa.cache.jvm.capacity";

    public final static String JVM_WEIGHT = "org.humbird.soa.cache.jvm.weight";

    public final static String JVM_LEVEL = "org.humbird.soa.cache.jvm.level";

    public final static String JVM_EXPIRATION = "org.humbird.soa.cache.jvm.expiration";

    public final static String MEMCACHED_SERVER = "org.humbird.soa.cache.memcached.server";

    public final static String MEMCACHED_POOL_SIZE = "org.humbird.soa.cache.memcached.poolsize";

    public final static String MEMCACHED_OPERATION_TIMEOUT = "org.humbird.soa.cache.memcached.operation.timeout";

    public final static String MEMCACHED_CONNECT_TIMEOUT = "org.humbird.soa.cache.memcached.connect.timeout";

    public final static String MEMCACHED_NAGLE = "org.humbird.soa.cache.memcached.nagle";

    /**
     * ################################################################################
     * ################################################################################
     * ################################################################################
     */

    public static Map<String, CacheContext> newJavaClient(String name, String path) {

        String CAPACITY = "512";
        String WEIGHT = "1024";
        String LEVEL = "32";
        String EXPIRATION = "0";

        Map<String, CacheContext> cacheManager;
        Properties properties = new Properties();
        try {
            properties.load(JAVACacheWrapper.class.getResourceAsStream(path));
            CAPACITY = properties.getProperty(CacheUtil.JVM_CAPACITY, CAPACITY);
            WEIGHT = properties.getProperty(CacheUtil.JVM_WEIGHT, WEIGHT);
            LEVEL = properties.getProperty(CacheUtil.JVM_LEVEL, LEVEL);
            EXPIRATION = properties.getProperty(CacheUtil.JVM_EXPIRATION, EXPIRATION);
        } catch (IOException e) {
            // ... warning
        }
        cacheManager = new ConcurrentLinkedHashMap.Builder<String, CacheContext>()
                .initialCapacity(Integer.parseInt(CAPACITY))
                .maximumWeightedCapacity(Long.parseLong(WEIGHT))
                .concurrencyLevel(Integer.parseInt(LEVEL))
                .build();
        cacheManager.put(name, new CacheContext(Integer.parseInt(EXPIRATION)));

        return cacheManager;
    }

    public static XMemcachedClient newMemcachedClient(String path) throws Exception {

        String URI = "127.0.0.1:11211";

        int poolSize = 4;

        int operationTimeOut = 6000;

        boolean nagle = false;

        int connectTimeOut = 2000;

        Properties properties = new Properties();
        try {
            properties.load(JAVACacheWrapper.class.getResourceAsStream(path));
            URI = properties.getProperty(CacheUtil.MEMCACHED_SERVER, URI);
            poolSize = Integer.parseInt(properties.getProperty(CacheUtil.MEMCACHED_POOL_SIZE, String.valueOf(poolSize)));
            operationTimeOut = Integer.parseInt(properties.getProperty(CacheUtil.MEMCACHED_OPERATION_TIMEOUT, String.valueOf(operationTimeOut)));
            nagle = Boolean.parseBoolean(properties.getProperty(CacheUtil.MEMCACHED_NAGLE, String.valueOf(nagle)));
            connectTimeOut = Integer.parseInt(properties.getProperty(CacheUtil.MEMCACHED_CONNECT_TIMEOUT, String.valueOf(connectTimeOut)));
        } catch (IOException e) {
            // ... warning
        }

        XMemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(URI));
        builder.setCommandFactory(new BinaryCommandFactory());
        builder.setConnectTimeout(connectTimeOut);
        builder.setConnectionPoolSize(poolSize);
        XMemcachedClient client= (XMemcachedClient) builder.build();
        client.setOpTimeout(operationTimeOut);
        client.setFailureMode(nagle);

        return client;
    }

    public static WebApplicationContext getCtx() {
        return ctx;
    }

    public static void newInstance(WebApplicationContext context) {
        if(ctx == null) {
            ctx = context;
        }
    }
}
