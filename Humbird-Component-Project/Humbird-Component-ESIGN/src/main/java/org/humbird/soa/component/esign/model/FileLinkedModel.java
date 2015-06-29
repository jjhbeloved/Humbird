package org.humbird.soa.component.esign.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15/4/7.
 */
public class FileLinkedModel {

    private String trueName;

    private List<String> subNames = new ArrayList();

    private File file;

    private String sub;

    public String getTrueName()
    {
        return this.trueName;
    }

    public void setTrueName(String trueName) {
        this.trueName = trueName;
    }

    public List<String> getSubNames() {
        return this.subNames;
    }

    public void setSubNames(List<String> subNames) {
        this.subNames = subNames;
    }

    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getSub() {
        return this.sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }
}
