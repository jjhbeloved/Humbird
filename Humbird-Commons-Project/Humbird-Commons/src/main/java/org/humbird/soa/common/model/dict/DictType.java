package org.humbird.soa.common.model.dict;

/**
 * Created by david on 15/3/16.
 */
public interface DictType {

    // 计算hash
    int hashFunction(Object key);

    // 复制key
    void keyDup(Object privatedata, Object key);

    // 复制val
    void valDup(Object privatedata, Object val);

    // 比较key
    void keyCompare(Object privatedata, Object key, Object val);

    // 销毁key
    void keyDestroy(Object privatedata, Object key);

    // 销毁val
    void valDestroy(Object privatedata, Object key);
}
