package org.hswebframework.payment.demo.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class IDGenerator {

    public static String generateId() {
        return DigestUtils.md5Hex(UUID.randomUUID().toString().concat(String.valueOf(Math.random())));
    }
}
