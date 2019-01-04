package org.hswebframework.payment.api.concurrent;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface DuplicateValidatorManager {
    DuplicateValidator getValidator(String type);
}
