package org.hswebframework.payment.api.crypto;

/**
 * 加密管理器,用于管理不同商户的加密规则
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface CipherManager {
    Cipher getCipher(Cipher.Type type, String merchantId);

    Signature getSignature(Signature.Type type, String merchantId);
}
