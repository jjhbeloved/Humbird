package org.humbird.soa.common.exception;

/**
 * Created by david on 15/2/6.
 */
public class FileOperationException extends Exception {

    public FileOperationException(String detail) {
        super(detail);
    }

    public FileOperationException(Exception e) {
        super(e);
    }

    public FileOperationException(String detail, Exception e) {
        super(detail, e);
    }
}
