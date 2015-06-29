package org.humbird.soa.core.util;

/**
 * Created by david on 15/3/20.
 */
public class KeyUtil {

    //  URL
    //  /index.xhtml session id
    public final static String INDEX_KEY = "#org#humbird#soa#index";
    //  /custom/custom.xhtml id
    public final static String CUSTOM_KEY = "#org#humbird#soa#custom#custom";

    //  core uri
    public final static String DEFAULT_VERSION = "org.humbird.soa.version";

    public final static String DEFAULT_FOLDER = "org.humbird.soa.custom.folder";

    public final static String DEFAULT_FOLDER_PATH = "org.humbird.soa.custom.folder.path";

    public final static String DEFAULT_CLUSTER = "org.humbird.soa.cluster";

    public final static String DEFAULT_METADATA_CACHE = "org.humbird.soa.metadata.cache";

    public final static String DEFAULT_LOCAL_CACHE = "org.humbird.soa.local.cache";

    public final static String DEFAULT_METADATA_CACHE_PATH = "org.humbird.soa.metadata.path";

    public final static String DEFAULT_LOCAL_CACHE_PATH = "org.humbird.soa.local.path";

    public final static String METADATA_EXPIRATION = "org.humbird.soa.metadata.expiration";

    public final static String DEFAULT_ENCRYPT = "org.humbird.soa.encrypt";

    //  cluster: master or slaves
    public final static String MASTER = "master";

    public final static String SLAVE = "slave";

    //  metadata cache: redis, memcached
    public final static String METADATA_CACHE_REDIS = "redis";

    public final static String METADATA_CACHE_MEMCACHED = "memcached";

    //  local cache: ehcache, jvm
    public final static String LOCAL_CACHE_EHCACHE = "ehcache";

    public final static String LOCAL_CACHE_JVM = "jvm";

    public static final String DEFAULT_EHCACHE_PATH = "/ehcache.xml";

    public static final String DEFAULT_JVM_PATH = "/jvm.properties";

    public static final String DEFAULT_MEMCACHED_PATH = "/xmemcached.properties";

    public static final String ENCRPYT = "3";
}
