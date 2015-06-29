package org.humbird.soa.common.exception;

/**
 * https://github.com/orgs/Asiainfo-ODC
 * 基础异常 : 此项目所有异常基于此对象
 *
 * Created by david on 14/12/15.
 */
public class BaseException extends Exception {

    private static final long serialVersionUID = 530246681047770468L;

    public BaseException(String detail, Exception e) {
        super(detail, e);
    }

    public BaseException(Exception e, Class clazz) {
        this(e.getMessage(), clazz);
    }

    public BaseException(String detail, Class clazz) {
        super(clazz.getName() + " -- " + detail);
    }

    public BaseException(String detail) {
        super(detail);
    }

    public BaseException(Exception e) {
        super(e);
    }
}
