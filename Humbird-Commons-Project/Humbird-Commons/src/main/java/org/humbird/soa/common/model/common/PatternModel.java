package org.humbird.soa.common.model.common;

import org.humbird.soa.common.tools.PatternLayout;
import org.humbird.soa.common.tools.TPatternLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15/3/17.
 */
public class PatternModel implements Serializable {

    private PatternLayout patternLayout = new TPatternLayout();

    private String clazzName = "org.humbird.soa.common.tools.TPatternLayout";

    private String sub = "_";

    private List vals = new ArrayList();

    public PatternLayout getPatternLayout() {
        return patternLayout;
    }

    public void setPatternLayout(PatternLayout patternLayout) {
        this.patternLayout = patternLayout;
    }

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public List getVals() {
        return vals;
    }

    public void setVals(List vals) {
        this.vals = vals;
    }

    public void clear() {
        vals.clear();
    }
}
