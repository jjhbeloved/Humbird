package org.humbird.soa.common.model.dict;

import java.io.Serializable;

/**
 * Created by david on 15/3/16.
 */
public class DictModel implements Serializable {

    //  private data
    private Object privatedata;

    //  hash table, array count is 2
    private DictHashModel ht[] = new DictHashModel[2];

    //  rehash index
    //  if not rehash, defautl is -1
    private int trehashidx = -1;

    // function type
    public DictType type;

    public DictModel() {
    }

    public DictModel(Object privatedata, DictHashModel[] ht, int trehashidx, DictType type) {
        this.privatedata = privatedata;
        this.trehashidx = trehashidx;
        this.type = type;
    }

    public Object getPrivatedata() {
        return privatedata;
    }

    public void setPrivatedata(Object privatedata) {
        this.privatedata = privatedata;
    }

    public DictHashModel[] getHt() {
        return ht;
    }

    public int getTrehashidx() {
        return trehashidx;
    }

    public void setTrehashidx(int trehashidx) {
        this.trehashidx = trehashidx;
    }

    public void setType(DictType type) {
        this.type = type;
    }
}
