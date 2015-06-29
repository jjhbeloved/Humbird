package org.humbird.soa.cache.data;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by david on 15/3/19.
 */
public interface Cache<K, V> {

    static final String MemcachedClientBuilder = "memcachedClientBuilder";

    static final String MemcachedClient = "memcachedClient";

    public void init(File file) throws Exception;

    public void put(String paramK, HumbirdSession paramV) throws Exception;

    public void put(Map<String, HumbirdSession> params) throws Exception;

    public HumbirdSession get(String paramK) throws Exception;

    public List getKeys() throws Exception;

    public boolean remove(String paramK) throws Exception;

    public boolean clear() throws Exception;

    public int size();

    public boolean close() throws Exception;
}
