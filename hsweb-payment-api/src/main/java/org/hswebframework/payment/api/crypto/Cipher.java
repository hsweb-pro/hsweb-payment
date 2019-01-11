package org.hswebframework.payment.api.crypto;

/**
 * 加密工具
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface Cipher {

    enum Type {
        NONE,
        RSA,
        AES,
        DES
    }

    /**
     * 加密类型,rsa
     *
     * @return 加密类型
     */
    Type getCipherType();

    /**
     * 对明文数据进行加密
     *
     * @param plaintext 明文数据
     * @return 密文
     */
    byte[] encrypt(byte[] plaintext);

    /**
     * 对密文进行解密
     *
     * @param cipherText 密文
     * @return 解密后的数据
     */
    byte[] decrypt(byte[] cipherText);
}
