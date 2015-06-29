package org.humbird.soa.portal.view;

import java.io.Serializable;

/**
 * 展示文件对象
 *
 * Created by david on 15/3/20.
 */
public class FolderModel implements Serializable {

    private String folder;

    private String file;

    private String path;

    private String index;

    public FolderModel(String folder, String file, String path, String index) {
        this.folder = folder;
        this.file = file;
        this.path = path;
        this.index = index;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
