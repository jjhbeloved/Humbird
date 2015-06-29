package org.humbird.soa.common.model.dict;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by david on 15/3/16.
 */
public class DictHashModel implements Serializable {

    // 映射表
    private Map table;

    // entry 个数
    private int size;

    // hash mask
    private int sizemask;

    // entry 已用个数
    private int used;

    public DictHashModel() {
    }

    public DictHashModel(Map table, int size, int sizemask, int used) {
        this.table = table;
        this.size = size;
        this.sizemask = sizemask;
        this.used = used;
    }

    public Map getTable() {
        return table;
    }

    public void setTable(Map table) {
        this.table = table;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSizemask() {
        return sizemask;
    }

    public void setSizemask(int sizemask) {
        this.sizemask = sizemask;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }
}
