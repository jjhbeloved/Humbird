package org.humbird.soa.common.exception;

/**
 * Created by david on 15/2/6.
 */
public class InitUriException extends Exception {

    public InitUriException(String detail) {
        super(detail);
    }

    public InitUriException(Exception e) {
        super(e);
    }

    public InitUriException(String detail, Exception e) {
        super(detail, e);
    }
}
