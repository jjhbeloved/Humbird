package org.humbird.soa.component.esign.model;

/**
 * Created by david on 15/4/7.
 */
public class DocDateModel {

    private int nxmlDate = 9;

    private int npdfDate = 9;

    public int getNxmlDate() {
        return this.nxmlDate;
    }

    public void setNxmlDate(int nxmlDate) {
        this.nxmlDate = nxmlDate;
    }

    public int getNpdfDate() {
        return this.npdfDate;
    }

    public void setNpdfDate(int npdfDate) {
        this.npdfDate = npdfDate;
    }
}
