package org.humbird.soa.common.codec;

import java.util.List;

/**
 * Created by david on 15/3/9.
 */
public interface CodecCallback {
    List encrypt(String key, String old);
}
