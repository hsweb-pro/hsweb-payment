package org.hswebframework.payment.api.merchant.config;

import java.util.List;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0.0
 */
class NullMerchantConfigHolder implements MerchantConfigHolder {
    @Override
    public <T> Optional<List<T>> asList(Class<T> t) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> as(Class<T> t) {
        return Optional.empty();
    }

    @Override
    public Optional<String> asString() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> asLong() {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> asInt() {
        return Optional.empty();
    }

    @Override
    public Optional<Double> asDouble() {
        return Optional.empty();
    }

    @Override
    public Optional<Object> getValue() {
        return Optional.empty();
    }
}
