package org.humbird.soa.common.exception;

/**
 * Created by david on 15/2/6.
 */
public class ConnectionException extends Exception {

    public ConnectionException(String detail) {
        super(detail);
    }

    public ConnectionException(Exception e) {
        super(e);
    }

    public ConnectionException(String detail, Exception e) {
        super(detail, e);
    }
}
