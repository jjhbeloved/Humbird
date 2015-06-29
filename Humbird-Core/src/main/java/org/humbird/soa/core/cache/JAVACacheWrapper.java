package org.humbird.soa.core.cache;


import org.humbird.soa.core.HumbirdSession;
import org.humbird.soa.core.util.CacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by david on 15/3/19.
 */
public class JAVACacheWrapper<K, V>
        implements Cache<K, V> {

    private final static Logger LOGGER = LoggerFactory.getLogger(JAVACacheWrapper.class);

    private final Map<String, CacheContext> cacheManager;

    private String cacheName;

    public JAVACacheWrapper(String cacheName, String path) {

        this.cacheName = cacheName;
        this.cacheManager = CacheUtil.newJavaClient(cacheName, path);
    }

    CacheContext<K> getCache() {
        return this.cacheManager.get(this.cacheName);
    }

    @Override
    public void addCache(String name, Object params) {
        if(params instanceof  CacheContext) {
            cacheManager.put(name, (CacheContext<K>) params);
            LOGGER.debug("added cache {0}", name);
        }
    }

    @Override
    public void switchCache(String name) {
        this.cacheName = name;
    }

    @Override
    public void delCache(String name) {
        cacheManager.remove(name);
    }

    @Override
    public void clearCache() {
        cacheManager.clear();
    }

    public void put(K key, V value) {
        CacheContext<K> cacheContext = getCache();
        if(!cacheContext.isExpired()) {
            HumbirdSession session = new HumbirdSession(key, value);
            cacheContext.getMap().put(key, session);
        } else {
            cacheContext.getMap().clear();
        }
    }

    @Override
    public void putOnlyOne(K paramK, V paramV) throws Exception {

    }

    public V get(K key) {
        CacheContext<K> cacheContext = getCache();
        if(cacheContext.isExpired()) {
            getCache().getMap().clear();
            LOGGER.debug("cache {0} is expired", key);
            return null;
        } else {
            HumbirdSession session = cacheContext.getMap().get(key);
            if (session != null) {
                return (V) session.getValue();
            }
            return null;
        }
    }

    public boolean remove(K key) {
        if(getCache().getMap().remove(key) != null) {
            LOGGER.debug("removed cache {0}", key);
            return true;
        } else {
            return false;
        }
    }

    public List getKeys() {
        List list = new ArrayList();
        Iterator<Map.Entry<K, HumbirdSession>> iterator = getCache().getMap().entrySet().iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next().getValue());
        }
        return list;
    }

    public void removeAll() {

        getCache().getMap().clear();
    }

    @Override
    public int size() {
        return getCache().getMap().size();
    }
}