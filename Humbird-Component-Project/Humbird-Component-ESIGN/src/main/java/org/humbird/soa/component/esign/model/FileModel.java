package org.humbird.soa.component.esign.model;

import java.io.BufferedWriter;
import java.io.File;

/**
 * Created by david on 15/4/7.
 */
public class FileModel {

    private String rootDir;

    private File mappingDir;

    private File dataDir;

    private File failedDir;

    private File bakDir;

    private File tarDir;

    private File gzDir;

    private File putDir;

    private File lazDir;

    private BufferedWriter lazBuffer;

    public String getRootDir()
    {
        return this.rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public File getMappingDir() {
        return this.mappingDir;
    }

    public void setMappingDir(File mappingDir) {
        this.mappingDir = mappingDir;
    }

    public File getDataDir() {
        return this.dataDir;
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    public File getFailedDir() {
        return this.failedDir;
    }

    public void setFailedDir(File failedDir) {
        this.failedDir = failedDir;
    }

    public File getBakDir() {
        return this.bakDir;
    }

    public void setBakDir(File bakDir) {
        this.bakDir = bakDir;
    }

    public File getTarDir() {
        return this.tarDir;
    }

    public void setTarDir(File tarDir) {
        this.tarDir = tarDir;
    }

    public File getGzDir() {
        return this.gzDir;
    }

    public void setGzDir(File gzDir) {
        this.gzDir = gzDir;
    }

    public File getPutDir() {
        return this.putDir;
    }

    public void setPutDir(File putDir) {
        this.putDir = putDir;
    }

    public File getLazDir() {
        return this.lazDir;
    }

    public void setLazDir(File lazDir) {
        this.lazDir = lazDir;
    }

    public BufferedWriter getLazBuffer() {
        return this.lazBuffer;
    }

    public void setLazBuffer(BufferedWriter lazBuffer) {
        this.lazBuffer = lazBuffer;
    }
}
