package org.humbird.soa.core.exceptions;

/**
 * Created by david on 15/3/19.
 */
public enum ServiceError
{
    APPLICATION_ERROR(500, "Something bad happend!");

    private final int code;
    private final String text;

    private ServiceError(int errorCode, String errorText) { this.code = errorCode;
        this.text = errorText; }

    public int getCode()
    {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public String getMessage() {
        return getCode() + ":" + getText();
    }
}