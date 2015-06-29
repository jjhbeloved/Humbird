package org.humbird.soa.core.cache;

import java.util.List;

/**
 * Created by david on 15/3/19.
 */
public abstract interface Cache<K, V> {

    public abstract void addCache(String name, Object params) throws Exception;

    public abstract void switchCache(String name);

    public abstract void delCache(String name) throws Exception;

    public abstract void clearCache();

    public abstract void put(K paramK, V paramV) throws Exception;

    public abstract void putOnlyOne(K paramK, V paramV) throws Exception;

    public abstract V get(K paramK) throws Exception;

    public abstract boolean remove(K paramK) throws Exception;

    public abstract List getKeys() throws Exception;

    public abstract void removeAll();

    public abstract int size() throws Exception;
}
