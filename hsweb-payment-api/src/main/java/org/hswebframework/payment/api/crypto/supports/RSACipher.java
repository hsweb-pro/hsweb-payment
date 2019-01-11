package org.hswebframework.payment.api.crypto.supports;

import org.hswebframework.payment.api.crypto.Cipher;
import org.hswebframework.payment.api.utils.UrlUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.hswebframework.payment.api.crypto.Signature;

import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class RSACipher implements Cipher, Signature {

    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    /**
     * RSA最大加密明文块大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;
    /**
     * RSA最大解密密文块大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    private static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    private static KeyFactory keyFactory;

    static {
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Signature.Type getSignType() {
        return Signature.Type.MD5withRSA;
    }

    @Override
    public Cipher.Type getCipherType() {
        return Cipher.Type.RSA;
    }

    @SneakyThrows
    public static RSAKeyInfo generateKey(int keySize) {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(keySize);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAKeyInfo info = new RSAKeyInfo();
        info.setPublicKey(Base64.encodeBase64String(keyPair.getPublic().getEncoded()));
        info.setPrivateKey(Base64.encodeBase64String(keyPair.getPrivate().getEncoded()));
        return info;
    }

    public RSACipher(String publicKey, String privateKey) throws Exception {
        this(keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(publicKey))),
                keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey))));
    }

    public RSACipher(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    @SneakyThrows
    public String sign(Object plaintext) {
        java.security.Signature signature = java.security.Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(plaintext instanceof byte[] ? ((byte[]) plaintext) : UrlUtils.objectToUrlParameters(plaintext).getBytes());
        return Base64.encodeBase64String(signature.sign());
    }

    @Override
    @SneakyThrows
    public boolean verify(String sign, Object plaintext) {
        java.security.Signature signature = java.security.Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(plaintext instanceof byte[] ? ((byte[]) plaintext) :UrlUtils.objectToUrlParameters(plaintext).getBytes());
        return signature.verify(Base64.decodeBase64(sign));
    }

    @Override
    @SneakyThrows
    public byte[] encrypt(byte[] plaintext) {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, privateKey);
        return doFinal(plaintext, cipher, MAX_ENCRYPT_BLOCK);
    }

    @Override
    @SneakyThrows
    public byte[] decrypt(byte[] cipherText) {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, publicKey);
        return doFinal(cipherText, cipher, MAX_DECRYPT_BLOCK);
    }

    private byte[] doFinal(byte[] data, javax.crypto.Cipher cipher, int max) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int inputLen = data.length;
            int offSet = 0;
            byte[] cache;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > max) {
                    cache = cipher.doFinal(data, offSet, max);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * max;
            }
            return out.toByteArray();
        }
    }

}
