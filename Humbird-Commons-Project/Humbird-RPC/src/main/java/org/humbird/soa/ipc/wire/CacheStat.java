package org.humbird.soa.ipc.wire;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by david on 15/6/12.
 */
public class CacheStat {

    private int id = 0;

    private boolean updated = true;

    private long lastDate = System.currentTimeMillis();

    public volatile static AtomicInteger needUpdateCount = new AtomicInteger(0);  // 需要变更个数

    public volatile static AtomicInteger updatedCount = new AtomicInteger(0);    // 已变更个数

    public CacheStat() {
        needUpdateCount.incrementAndGet();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
        if(updated) {
            updatedCount.decrementAndGet();
        } else {
            updatedCount.incrementAndGet();
        }
    }

    public long getLastDate() {
        return lastDate;
    }

    public void setLastDate(long lastDate) {
        this.lastDate = lastDate;
    }

}
