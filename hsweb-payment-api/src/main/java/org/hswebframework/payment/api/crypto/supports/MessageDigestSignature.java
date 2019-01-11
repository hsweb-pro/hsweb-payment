package org.hswebframework.payment.api.crypto.supports;

import com.google.common.primitives.Bytes;
import org.hswebframework.payment.api.crypto.Signature;
import org.hswebframework.payment.api.utils.UrlUtils;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.util.function.BiFunction;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class MessageDigestSignature implements Signature {

    private byte[] key;

    private MessageDigest messageDigest;

    public static final BiFunction<Object, byte[], byte[]> DEFAULT_ENCODER = new UrlEncoder();

    private BiFunction<Object, byte[], byte[]> encoder = DEFAULT_ENCODER;

    private Type type;

    public MessageDigestSignature(MessageDigest digest, Type type, byte[] key) {
        this.key = key;
        this.messageDigest = digest;
        this.type = type;
    }

    public void setEncoder(BiFunction<Object, byte[], byte[]> encoder) {
        this.encoder = encoder;
    }

    @Override
    public Type getSignType() {
        return type;
    }

    @Override
    public String sign(Object plaintext) {
        messageDigest.update(encoder.apply(plaintext, key));
        return Hex.encodeHexString(messageDigest.digest()).toUpperCase();
    }

    @Override
    public boolean verify(String sign, Object plaintext) {
        return sign.equalsIgnoreCase(sign(plaintext));
    }


    public static class UrlEncoder implements BiFunction<Object, byte[], byte[]> {

        static byte[] appendKeyBytes = "&".getBytes();

        @Override
        public byte[] apply(Object target, byte[] key) {
            if (target instanceof byte[]) {
                return Bytes.concat(((byte[]) target), appendKeyBytes, key);
            }
            return Bytes.concat(UrlUtils.objectToUrlParameters(target, k->!"sign".equals(k)).getBytes(), appendKeyBytes, key);
        }
    }

}
