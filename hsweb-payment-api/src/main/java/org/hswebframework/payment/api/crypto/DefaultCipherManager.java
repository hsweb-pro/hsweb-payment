package org.hswebframework.payment.api.crypto;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DefaultCipherManager implements CipherManager {

    private Map<Cipher.Type, Cipher>       cipherMap    = new HashMap<>();
    private Map<Signature.Type, Signature> signatureMap = new HashMap<>();

    public DefaultCipherManager register(Cipher cipher) {
        cipherMap.put(cipher.getCipherType(), cipher);
        return this;
    }

    public DefaultCipherManager register(Signature signature) {
        signatureMap.put(signature.getSignType(), signature);
        return this;
    }

    @Override
    public Cipher getCipher(Cipher.Type type, String merchantId) {
        return cipherMap.get(type);
    }

    @Override
    public Signature getSignature(Signature.Type type, String merchantId) {
        return signatureMap.get(type);
    }
}
