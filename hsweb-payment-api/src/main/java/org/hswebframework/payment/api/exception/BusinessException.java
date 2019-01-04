package org.hswebframework.payment.api.exception;

import lombok.Getter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {
    private String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
