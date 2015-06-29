package org.humbird.soa.core.cache;

import net.sf.ehcache.util.TimeUtil;
import org.humbird.soa.core.HumbirdSession;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 15/4/5.
 */
public class CacheContext<K> implements Serializable {

    private int EXPIRATION = 0;

    private Map<K, HumbirdSession> map = new HashMap<K, HumbirdSession>();

    private volatile int timeToLive;

    private volatile int timeToIdle;

    private transient long creationTime;

    private transient long lastAccessTime;

    private volatile long lastUpdateTime;

    private volatile boolean cacheDefaultLifespan;

    public CacheContext() {
        this(0);
    }

    public CacheContext(int EXPIRATION) {
        this.EXPIRATION = EXPIRATION;
        this.timeToLive = EXPIRATION == 0 ? -2147483648 : EXPIRATION;
        this.timeToIdle = -2147483648;
        this.creationTime = this.getCurrentTime();
        this.cacheDefaultLifespan = true;
    }

    public int getEXPIRATION() {
        return EXPIRATION;
    }

    public void setEXPIRATION(int EXPIRATION) {
        this.EXPIRATION = EXPIRATION;
    }

    public Map<K, HumbirdSession> getMap() {
        return map;
    }

    public boolean isLifespanSet() {
        return this.timeToIdle != -2147483648 || this.timeToLive != -2147483648;
    }

    public long getExpirationTime() {
        if(this.isLifespanSet() && !this.isEternal()) {
            long expirationTime = 0L;
            long ttlExpiry = this.creationTime + TimeUtil.toMillis(this.getTimeToLive());
            long mostRecentTime = Math.max(this.creationTime, this.lastAccessTime);
            long ttiExpiry = mostRecentTime + TimeUtil.toMillis(this.getTimeToIdle());
            if(this.getTimeToLive() != 0 && (this.getTimeToIdle() == 0 || this.lastAccessTime == 0L)) {
                expirationTime = ttlExpiry;
            } else if(this.getTimeToLive() == 0) {
                expirationTime = ttiExpiry;
            } else {
                expirationTime = Math.min(ttlExpiry, ttiExpiry);
            }

            return expirationTime;
        } else {
            return 9223372036854775807L;
        }
    }

    public int getTimeToLive() {
        return -2147483648 == this.timeToLive?0:this.timeToLive;
    }

    public int getTimeToIdle() {
        return -2147483648 == this.timeToIdle?0:this.timeToIdle;
    }

    public boolean isEternal() {
        return 0 == this.timeToIdle && 0 == this.timeToLive;
    }

    long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public boolean isExpired() {
        if(this.isLifespanSet() && !this.isEternal()) {
            long now = this.getCurrentTime();
            long expirationTime = this.getExpirationTime();
            return now > expirationTime;
        } else {
            return false;
        }
    }

    public void setTimeToLive(int timeToLiveSeconds) {
        if(timeToLiveSeconds < 0) {
            throw new IllegalArgumentException("timeToLive can\'t be negative");
        } else {
            this.cacheDefaultLifespan = false;
            this.timeToLive = timeToLiveSeconds;
        }
    }
}
