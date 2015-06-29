package org.humbird.soa.core.model;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Created by david on 15/3/23.
 */
public class ProtocolModel {

    private String folder;

    private String file;

    private ServletRequest request;

    private ServletResponse response;

    public ProtocolModel() {
    }

    public ProtocolModel(String folder, String file, ServletRequest request, ServletResponse response) {
        this.folder = folder;
        this.file = file;
        this.request = request;
        this.response = response;
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

    public ServletRequest getRequest() {
        return request;
    }

    public void setRequest(ServletRequest request) {
        this.request = request;
    }

    public ServletResponse getResponse() {
        return response;
    }

    public void setResponse(ServletResponse response) {
        this.response = response;
    }
}
