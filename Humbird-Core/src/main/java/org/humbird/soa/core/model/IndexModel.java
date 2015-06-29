package org.humbird.soa.core.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

/**
 * Created by david on 15/3/20.
 */
public class IndexModel implements Serializable {

    private String version;

    private Properties properties;

    private Map values;

    public IndexModel() {
    }

    public IndexModel(String version, Properties properties, Map values) {
        this.version = version;
        this.properties = properties;
        this.values = values;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map getValues() {
        return values;
    }

    public void setValues(Map values) {
        this.values = values;
    }
}
