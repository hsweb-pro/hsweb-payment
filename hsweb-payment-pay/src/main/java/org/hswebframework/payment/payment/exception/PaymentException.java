package org.hswebframework.payment.payment.exception;

import org.hswebframework.payment.api.exception.BusinessException;
import lombok.Getter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
public class PaymentException extends BusinessException {

    public PaymentException(String code, String message) {
        super(code, message);
    }

    public PaymentException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
