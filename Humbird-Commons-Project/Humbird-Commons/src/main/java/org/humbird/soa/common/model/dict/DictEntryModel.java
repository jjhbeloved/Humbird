package org.humbird.soa.common.model.dict;

import java.io.Serializable;

/**
 * Created by david on 15/3/16.
 */
public class DictEntryModel implements Serializable {

    private Object key;

    private Object value;

    private DictEntryModel next;

    public DictEntryModel() {
    }

    public DictEntryModel(Object key, Object value, DictEntryModel next) {
        this.key = key;
        this.value = value;
        this.next = next;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public DictEntryModel getNext() {
        return next;
    }

    public void setNext(DictEntryModel next) {
        this.next = next;
    }
}
