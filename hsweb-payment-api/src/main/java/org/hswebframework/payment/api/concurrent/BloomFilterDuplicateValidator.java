package org.hswebframework.payment.api.concurrent;

import com.google.common.hash.BloomFilter;

import java.nio.charset.StandardCharsets;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class BloomFilterDuplicateValidator implements DuplicateValidator {

    private BloomFilter<String> filter = BloomFilter.create((v, s) -> s.putString(v, StandardCharsets.UTF_8), 1000, 0.01);

    @Override
    public boolean put(String unique) {
        return filter.put(unique);
    }

}
