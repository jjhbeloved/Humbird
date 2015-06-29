package org.humbird.soa.core.exceptions;

/**
 * Created by david on 15/3/19.
 */
public class ServiceException extends RuntimeException {

    private ServiceError error = ServiceError.APPLICATION_ERROR;

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(ServiceError error) {
        super(error.getMessage());
        this.error = error;
    }

    public ServiceException(ServiceError error, String message) {
        super(error.getMessage() + " " + message);
        this.error = error;
    }

    public ServiceException(ServiceError error, Throwable cause) {
        super(error.getMessage(), cause);
        this.error = error;
    }

    public ServiceError getError() {
        return this.error;
    }
}