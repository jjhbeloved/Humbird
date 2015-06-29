package org.humbird.soa.cache.data;

import net.rubyeye.xmemcached.XMemcachedClient;
import org.humbird.soa.common.utils.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Created by david on 15/6/4.
 */
class MEMCacheWrapper<K, V> implements Cache<K, V> {

    private final static Logger LOGGER = LoggerFactory.getLogger(MEMCacheWrapper.class);

    private static XMemcachedClient cacheClient = null;

    private static boolean flag = true;

    private Set<String> keySet = new HashSet<String>();

    @Override
    public void init(File file) throws Exception {

        if (flag) {
            cacheClient = (XMemcachedClient) SpringUtils.get().getBean(MemcachedClient);
            flag = false;
        }

    }

    @Override
    public void put(String paramK, HumbirdSession paramV) throws Exception {
        cacheClient.setWithNoReply(paramK, paramV.getSessionStick().getExpire(), paramV);
        keySet.add(paramK);
    }

    @Override
    public void put(Map<String, HumbirdSession> params) throws Exception {
        Iterator<Map.Entry<String, HumbirdSession>> iterator = params.entrySet().iterator();
        Map.Entry<String, HumbirdSession> entry;
        for (;iterator.hasNext();) {
            entry = iterator.next();
            HumbirdSession humbirdSession = entry.getValue();
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public HumbirdSession get(String paramK) throws Exception {
        return cacheClient.get(paramK);
    }

    @Override
    public List getKeys() throws Exception {
        List<String> keys = new ArrayList<String>();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            keys.add(iterator.next());
        }
        return keys;
    }

    @Override
    public boolean remove(String paramK) throws Exception{
        cacheClient.deleteWithNoReply(paramK);
        keySet.remove(paramK);
        return true;
    }

    @Override
    public boolean clear() throws Exception{
        cacheClient.flushAll();
        keySet.clear();
        return true;
    }

    @Override
    public int size() {
        return keySet.size();
    }

    @Override
    public boolean close() throws Exception {
        if (!flag && !cacheClient.isShutdown()) {
            clear();
            cacheClient.shutdown();
            flag = true;
        }
        return flag;
    }
}
