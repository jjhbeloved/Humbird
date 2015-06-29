package org.humbird.soa.component.esign.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by david on 15/4/7.
 */
public class StatModel {

    private String mode;

    private Map<File, Stat> stat = new HashMap();

    public StatModel() {
    }

    public StatModel(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return this.mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Map<File, Stat> getStat() {
        return this.stat;
    }

    public Stat newInstance() {
        return new Stat();
    }

    public static class Stat
    {
        private AtomicInteger stat = new AtomicInteger(-1);

        private AtomicBoolean ban = new AtomicBoolean(false);

        private AtomicBoolean first = new AtomicBoolean(true);

        public AtomicInteger getStat()
        {
            return this.stat;
        }

        public void setStat(AtomicInteger stat) {
            this.stat = stat;
        }

        public AtomicBoolean getBan() {
            return this.ban;
        }

        public void setBan(AtomicBoolean ban) {
            this.ban = ban;
        }

        public AtomicBoolean getFirst() {
            return this.first;
        }

        public void setFirst(AtomicBoolean first) {
            this.first = first;
        }
    }
}
