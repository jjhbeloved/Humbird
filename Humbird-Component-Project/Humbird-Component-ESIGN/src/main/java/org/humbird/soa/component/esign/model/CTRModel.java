package org.humbird.soa.component.esign.model;

import org.humbird.soa.common.model.common.MapModel;
import org.humbird.soa.common.tools.PatternLayout;
import org.humbird.soa.common.tools.TPatternLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15/4/7.
 */
public class CTRModel {

    private PatternLayout patternLayout;

    private String clazzName;

    private CTRSchema vaild;

    private CTRSchema invaild;

    public CTRModel() {
        this.patternLayout = new TPatternLayout();

        this.clazzName = "org.humbird.soa.common.tools.TPatternLayout";

        this.vaild = new CTRSchema();

        this.invaild = new CTRSchema();
    }

    public PatternLayout getPatternLayout() {
        return this.patternLayout;
    }

    public void setPatternLayout(PatternLayout patternLayout) {
        this.patternLayout = patternLayout;
    }

    public String getClazzName() {
        return this.clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public CTRSchema getVaild() {
        return this.vaild;
    }

    public CTRSchema getInvaild() {
        return this.invaild;
    }

    public void clear() {
        vaild.clear();
        invaild.clear();
    }

    public static class CTRSchema {
        private List<MapModel> split = new ArrayList<MapModel>();
        private List<MapModel> head = new ArrayList<MapModel>();
        private List<MapModel> body = new ArrayList<MapModel>();
        private List<MapModel> tail = new ArrayList<MapModel>();

        public List<MapModel> getSplit() {
            return this.split;
        }

        public void setSplit(List<MapModel> split) {
            for (MapModel mapModel : split) {
                this.split.add(mapModel);
            }
        }

        public List<MapModel> getHead() {
            return this.head;
        }

        public void setHead(List<MapModel> head) {
            for (MapModel mapModel : head) {
                this.head.add(mapModel);
            }
        }

        public List<MapModel> getBody() {
            return this.body;
        }

        public void setBody(List<MapModel> body) {
            for (MapModel mapModel : body) {
                this.body.add(mapModel);
            }
        }

        public List<MapModel> getTail() {
            return this.tail;
        }

        public void setTail(List<MapModel> tail) {
            for (MapModel mapModel : tail) {
                this.tail.add(mapModel);
            }
        }

        public void clear() {
            split.clear();
            head.clear();
            body.clear();
            tail.clear();
        }
    }
}
