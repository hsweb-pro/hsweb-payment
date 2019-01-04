package org.hswebframework.payment.api.concurrent;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Component
public class DefaultDuplicateValidatorManager implements DuplicateValidatorManager {
    private Map<String, DuplicateValidator> validatorMap = new ConcurrentHashMap<>();

    @Override
    public DuplicateValidator getValidator(String type) {
        return validatorMap.computeIfAbsent(type, t -> new BloomFilterDuplicateValidator());
    }
}
