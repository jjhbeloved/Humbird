package org.humbird.soa.common.exception;

/**
 * Created by david on 15/2/10.
 */
public class CrashException extends Exception {
    public CrashException(String detail) {
        super(detail);
    }

    public CrashException(Exception e) {
        super(e);
    }

    public CrashException(String detail, Exception e) {
        super(detail, e);
    }
}
