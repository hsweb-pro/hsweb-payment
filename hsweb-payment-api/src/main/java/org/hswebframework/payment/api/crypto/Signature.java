package org.hswebframework.payment.api.crypto;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface Signature {

    enum Type {
        MD5,
        SHA256,
        MD5withRSA
    }

    Type getSignType();

    /**
     * 验签
     *
     * @param data 明文数据
     * @return 验签是否成功
     */
    boolean verify(String sign, Object data);

    /**
     * 签名
     *
     * @param data 明文
     * @return 签名结果
     */
    String sign(Object data);
}
