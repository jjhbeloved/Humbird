package org.humbird.soa.common.model.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.Serializable;

/**
 * Created by david on 15/3/17.
 */
public class FBufferModel implements Serializable {

    private File file;

    private BufferedWriter bufferedWriter;

    private StringBuilder stringBuilder;

    private int count;

    public FBufferModel() {
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    public void setBufferedWriter(BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
    }

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    public void setStringBuilder(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
