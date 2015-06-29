package org.humbird.soa.core.cache;


import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by david on 15/3/19.
 */
public class EHCacheWrapper<K, V>
        implements
        Cache<K, V> {

    private final static Logger LOGGER = LoggerFactory.getLogger(EHCacheWrapper.class);

    private final CacheManager cacheManager;

    private String cacheName;

    public EHCacheWrapper(String cacheName, CacheManager cacheManager) {
        this.cacheName = cacheName;
        this.cacheManager = cacheManager;
    }

    Ehcache getCache() {
        return this.cacheManager.getEhcache(this.cacheName);
    }

    @Override
    public void addCache(String name, Object params) {
        if (params instanceof Ehcache) {
            this.cacheManager.addCache((Ehcache) params);
            LOGGER.debug("added cache {0}", name);
        }
    }

    @Override
    public void switchCache(String name) {
        this.cacheName = name;
    }

    @Override
    public void delCache(String name) {
        this.cacheManager.removeCache(name);
        LOGGER.debug("removed cache {0}", name);
    }

    @Override
    public void clearCache() {
        this.cacheManager.clearAll();
        LOGGER.debug("cleared all cache.");
    }

    public void put(K key, V value) {
        getCache().put(new Element(key, value));
    }

    @Override
    public void putOnlyOne(K paramK, V paramV) throws Exception {

    }

    public V get(K key) {
        Element element = getCache().get(key);
        if (element != null) {
            return (V) element.getObjectValue();
        }
        return null;
    }

    public boolean remove(K key) {
        return getCache().remove(key);
    }

    public List getKeys() {
        return getCache().getKeys();
    }

    public void removeAll() {
        getCache().removeAll();
        LOGGER.debug("all cache was clear");
    }

    @Override
    public int size() {
        return getCache().getSize();
    }
}