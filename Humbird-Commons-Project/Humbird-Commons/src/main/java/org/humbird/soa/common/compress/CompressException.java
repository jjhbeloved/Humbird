package org.humbird.soa.common.compress;

import org.humbird.soa.common.exception.BaseException;

/**
 * https://github.com/orgs/Asiainfo-ODC
 *
 * Created by david on 15/1/2.
 */
public class CompressException extends BaseException {
    private static final long serialVersionUID = -5700767483194052468L;

    public CompressException(String detail, Exception e) {
        super(detail, e);
    }

    public CompressException(String detail) {
        super(detail);
    }
}
