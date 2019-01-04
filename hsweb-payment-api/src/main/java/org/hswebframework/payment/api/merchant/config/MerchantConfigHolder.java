package org.hswebframework.payment.api.merchant.config;

import lombok.SneakyThrows;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface MerchantConfigHolder {

    MerchantConfigHolder NULL = new NullMerchantConfigHolder();

    <T> Optional<List<T>> asList(Class<T> t);

    <T> Optional<T> as(Class<T> t);

    Optional<String> asString();

    Optional<Long> asLong();

    Optional<Integer> asInt();

    Optional<Double> asDouble();

    Optional<Object> getValue();

    @SneakyThrows
    default MerchantConfigHolder assertPresent(Supplier<Exception> exceptionSupplier) {
        if (getValue().isPresent()) {
            throw exceptionSupplier.get();
        }
        return this;
    }
}
