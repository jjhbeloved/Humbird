package org.humbird.soa.common.model.common;

import org.humbird.soa.common.tools.TTimestamp;

import java.io.Serializable;

/**
 * Created by david on 15/3/18.
 */
public class MapModel implements Serializable {

    private final static char l = '{';
    private final static char r = '}';
    private final static char m = ':';

    public final static String DATE = "date";
    public final static String CONST = "const";
    public final static String RAND = "rand";
    public final static String INDEX = "index";

    private String type;

    private String value;

    public MapModel() {
    }

    public MapModel(String all) {
        int begin = all.indexOf(l);
        int end = all.lastIndexOf(r);
        int middle = all.indexOf(m);
        type = all.substring(begin + 1, middle);
        value = all.substring(middle + 1, end);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTrue(Callback callback) {
        if(CONST.equals(type)) {
            return value;
        } else if(DATE.equals(type)) {
            return TTimestamp.getDate(value);
        } else if(RAND.equals(type)) {
            return callback.done(type, value);
        } else if(INDEX.equals(type)) {
            return value;
        }
        return "";
    }

    public String any(Callback callback) {
        return callback.done(type, value);
    }

    public interface Callback {
        String done(String key, String value);
    }
}
