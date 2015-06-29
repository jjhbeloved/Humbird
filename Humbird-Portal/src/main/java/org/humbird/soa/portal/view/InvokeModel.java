package org.humbird.soa.portal.view;

import org.humbird.soa.core.invoke.Done;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by david on 15/3/23.
 */
public class InvokeModel implements Serializable {

    private Map<String, Done> sources;

    public InvokeModel() {
    }

    public Map<String, Done> getSources() {
        return sources;
    }

    public void setSources(Map<String, Done> sources) {
        this.sources = sources;
    }
}
