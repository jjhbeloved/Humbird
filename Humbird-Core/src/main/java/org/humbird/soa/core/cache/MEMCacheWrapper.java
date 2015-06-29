package org.humbird.soa.core.cache;

import net.rubyeye.xmemcached.XMemcachedClient;
import org.humbird.soa.core.util.CacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by david on 15/4/4.
 */
public class MEMCacheWrapper<K, V>
        implements Cache<K, V> {

    private final static Logger LOGGER = LoggerFactory.getLogger(MEMCacheWrapper.class);

    private final XMemcachedClient cacheManager;

    public MEMCacheWrapper(String path) throws Exception {
        this.cacheManager = CacheUtil.newMemcachedClient(path);
    }

    @Override
    public void addCache(String name, Object params) throws Exception{
        //
    }

    @Override
    public void switchCache(String name) {
        //
    }

    @Override
    public void delCache(String name) throws Exception{
        //
    }

    @Override
    public void clearCache() {
        // stop
    }

    @Override
    public void put(K paramK, V paramV) throws Exception {
        if(paramV instanceof CacheContext && paramK instanceof String) {
            CacheContext<K> cacheContext = (CacheContext<K>) paramV;
            cacheManager.set((String) paramK, cacheContext.getEXPIRATION(), cacheContext);
        }
    }

    @Override
    public void putOnlyOne(K paramK, V paramV) throws Exception {
        if(paramV instanceof CacheContext && paramK instanceof String) {
            String key = (String) paramK;
            V result = get(paramK);
            if(result == null) {
                CacheContext<K> cacheContext = (CacheContext<K>) paramV;
                cacheManager.set(key, cacheContext.getEXPIRATION(), cacheContext);
            }
        }
    }

    @Override
    public V get(K paramK) throws Exception{
        if(paramK instanceof String) {
            return cacheManager.get((String) paramK);
        }
        return null;
    }

    @Override
    public boolean remove(K paramK) throws Exception{
        if(paramK instanceof String) {
            if(cacheManager.delete((String) paramK)) {
                LOGGER.debug("removed cache {0}", paramK);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public List getKeys() throws Exception{
        List list = new ArrayList();
        Map<InetSocketAddress, Map<String, String>> stats = cacheManager.getStats();
        Iterator<Map.Entry<InetSocketAddress, Map<String, String>>> iterator = stats.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<InetSocketAddress, Map<String, String>> entry = iterator.next();
            Iterator<Map.Entry<String, String>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                list.add(iter.next().getKey());
            }
        }
        return list;
    }

    @Override
    public void removeAll() {
        // stop
    }

    @Override
    public int size() throws Exception {
        return getKeys().size();
    }
}