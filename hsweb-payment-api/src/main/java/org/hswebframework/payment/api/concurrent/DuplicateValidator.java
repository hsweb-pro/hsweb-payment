package org.hswebframework.payment.api.concurrent;

import org.springframework.dao.DuplicateKeyException;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface DuplicateValidator {

    default void tryPut(String unique, String message) throws DuplicateKeyException {
        if (!put(unique)) {
            throw new DuplicateKeyException(message);
        }
    }

    boolean put(String unique);

}
