package org.humbird.soa.cache.data;

import org.humbird.soa.cache.server.quorum.QuorumPeerConfig.CacheType;

import java.io.File;

/**
 * Created by david on 15/6/5.
 */
public class CacheReferee {

    private Cache cache = null;

    public void createCacheWrapper(CacheType cacheType, File cachePropFile) throws Exception {
        switch (cacheType) {
            case MEMCACHED: cache = new MEMCacheWrapper(); break;
            case REDIS:
                break;
            default:throw new Exception("not choose any cache type. error.");
        }
        cache.init(cachePropFile);
    }

    public Cache getCache() {
        return cache;
    }
}
