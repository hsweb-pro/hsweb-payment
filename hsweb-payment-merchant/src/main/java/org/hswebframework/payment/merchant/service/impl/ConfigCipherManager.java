package org.hswebframework.payment.merchant.service.impl;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.merchant.config.MerchantConfigHolder;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.crypto.Cipher;
import org.hswebframework.payment.api.crypto.CipherManager;
import org.hswebframework.payment.api.crypto.Signature;
import org.hswebframework.payment.api.crypto.supports.MessageDigestSignature;
import org.hswebframework.payment.api.enums.MerchantConfigKey;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Component
public class ConfigCipherManager implements CipherManager {

    @Autowired
    private MerchantConfigManager merchantConfigManager;

    private Map<String, Map<Signature.Type, Signature>> stringCache = new ConcurrentHashMap<>();

    @Override
    public Cipher getCipher(Cipher.Type type, String merchantId) {
        return null;
    }

    @Override
    public Signature getSignature(Signature.Type type, String merchantId) {
        if (StringUtils.isEmpty(merchantId)) {
            return null;
        }
        return stringCache
                .computeIfAbsent(merchantId, k -> new HashMap<>())
                .computeIfAbsent(type, k -> {
                    MerchantConfigHolder holder = merchantConfigManager
                            .getConfig(merchantId, MerchantConfigKey.SECRET_KEY.getValue());
                    byte[] key = holder
                            .asString()
                            .map(String::getBytes)
                            .orElseThrow(ErrorCode.MERCHANT_CONFIG_ERROR::createException);
                    switch (type) {
                        case MD5:
                            return new MessageDigestSignature(DigestUtils.getMd5Digest(), type, key);
                        case SHA256:
                            return new MessageDigestSignature(DigestUtils.getSha256Digest(), type, key);
                        default:
                            throw ErrorCode.MERCHANT_CONFIG_ERROR.createException();
                    }
                });
    }
}
