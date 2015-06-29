package org.humbird.soa.component.esign.codec;

import org.humbird.soa.common.codec.Codec;
import org.humbird.soa.common.codec.CodecCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15/4/7.
 */
public class ESignCallbackImpl implements CodecCallback {

    @Deprecated
    public List encrypt(String key, String old) {
        return vals(key, old);
    }

    @Deprecated
    private List vals(String key, String old) {
        try {
            List vals = new ArrayList();
            if (("org.humbird.esign.path".equalsIgnoreCase(key)) || ("org.humbird.esign.tmp.dir".equals(key)) || ("password".equalsIgnoreCase(key)) || ("org.humbird.esign.from.scaned.dir.uri".equals(key)) || ("org.humbird.esign.from.scan.dir.uri".equals(key)) || ("org.humbird.esign.error.uri".equals(key)) || ("org.humbird.esign.to.uri".equals(key))) {
                return Codec.getTrue(old, vals);
            }
            return null;
        } catch (Exception e) {
        }
        return null;
    }
}