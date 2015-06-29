package org.humbird.soa.cache.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 15/6/4.
 */
public class HumbirdSession<K, V> implements Serializable {

    private static final long serialVersionUID = -7763817066948801429L;
    private K key;

    private V value;

    private SessionStick sessionStick = new SessionStick();

    public static Map<Boolean, Map<String, HumbirdSession>> doubleCache = new HashMap<Boolean, Map<String, HumbirdSession>>();

    private static Map<String, HumbirdSession> session1 = new HashMap<String, HumbirdSession>();

    private static Map<String, HumbirdSession> session2 = new HashMap<String, HumbirdSession>();

    public HumbirdSession(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public SessionStick getSessionStick() {
        return sessionStick;
    }

    public void clear() {
        this.key = null;
        this.value = null;
        this.sessionStick = null;
    }

    public class SessionStick implements Serializable {

        private static final long serialVersionUID = -827906608393485793L;

        long createDate = System.currentTimeMillis();

        int expire;

        long lastsave;

        long lastaccess;

        public int getExpire() {
            return expire;
        }

        public void setExpire(int expire) {
            this.expire = expire;
        }

        public long getLastsave() {
            return lastsave;
        }

        public void setLastsave(long lastsave) {
            this.lastsave = lastsave;
        }

        public long getLastaccess() {
            return lastaccess;
        }

        public void setLastaccess(long lastaccess) {
            this.lastaccess = lastaccess;
        }
    }

    public static void main(String []args) {
        HumbirdSession session = new HumbirdSession(1, "3");
        System.out.println(session.getKey());
        System.out.println(session.getValue());
    }
}
