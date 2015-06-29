package org.humbird.soa.component.esign.model;

import org.humbird.soa.common.model.common.MapModel;
import org.humbird.soa.common.tools.TTimestamp;

import java.util.List;

/**
 * Created by david on 15/4/7.
 */
public class CTRCallback implements MapModel.Callback {

    private List<String> ns;

    private String nxml;

    private String npdf;

    public CTRCallback(List<String> ns, String nxml, String npdf) {
        this.ns = ns;
        this.nxml = nxml;
        this.npdf = npdf;
    }

    public String done(String type, String value) {
        if ("const".equals(type))
            return value;
        if ("date".equals(type))
            return TTimestamp.getDate(value);
        if ("rand".equals(type)) {
            if ("npdf".equals(value))
                return this.npdf;
            if ("nxml".equals(value)) {
                return this.nxml;
            }
            return "";
        }
        if ("index".equals(type)) {
            return (String) this.ns.get(Integer.parseInt(value));
        }
        return "";
    }
}
