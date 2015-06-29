package org.humbird.soa.core.model;

import org.humbird.soa.common.model.common.PropsModel;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by david on 15/3/20.
 */
public class PropertyModel implements Serializable {

    private String folder;

    private String file;

    private String key;

    private String path;

    private int encrpyt;

    private Properties properties;

    private PropsModel propsModel;

    public PropertyModel() {
    }

    public PropertyModel(String folder, String file, String key, PropsModel propsModel) {
        this(folder, file, key, propsModel, -1);
    }

    public PropertyModel(String folder, String file, String key, PropsModel propsModel, int encrpyt) {
        this.folder = folder;
        this.file = file;
        this.key = key;
        this.path = '/' + folder + '/' + file;
        this.propsModel = propsModel;
        this.encrpyt = encrpyt;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public PropsModel getPropsModel() {
        return propsModel;
    }

    public void setPropsModel(PropsModel propsModel) {
        this.propsModel = propsModel;
    }

    public int getEncrpyt() {
        return encrpyt;
    }

    public void setEncrpyt(int encrpyt) {
        this.encrpyt = encrpyt;
    }
}
