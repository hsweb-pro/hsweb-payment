package org.hswebframework.payment.api.crypto;

import org.hswebframework.payment.api.crypto.supports.RSACipher;
import org.hswebframework.payment.api.crypto.supports.RSAKeyInfo;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RSACipherTest {


    @Test
    public void testEnc() throws Exception {
        RSAKeyInfo serverKey = RSACipher.generateKey(1024);
        RSAKeyInfo clientKey = RSACipher.generateKey(1024);

        System.out.println("server public key:");
        System.out.println(serverKey.getPublicKey());
        System.out.println("server private key:");
        System.out.println(serverKey.getPrivateKey());

        System.out.println("client public key:");
        System.out.println(clientKey.getPublicKey());
        System.out.println("client private key:");
        System.out.println(clientKey.getPrivateKey());

        RSACipher serverCipher = new RSACipher(clientKey.getPublicKey(), serverKey.getPrivateKey());
        RSACipher clientCipher = new RSACipher(serverKey.getPublicKey(), clientKey.getPrivateKey());

        //----------------服务端发送------------
        byte[] serverData = "test测试1234".getBytes();
        String serverSign = serverCipher.sign(serverData);
        System.out.println("服务端签名:");
        System.out.println(serverSign);
        byte[] serverEncData = serverCipher.encrypt(serverData);
        System.out.println("服务端加密:");
        System.out.println(Base64.encodeBase64String(serverEncData));
        //------------------客户端接收

        byte[] clientDec = clientCipher.decrypt(serverEncData);
        boolean clientVerity = clientCipher.verify(serverSign, clientDec);

        System.out.println("客户端验签:" + clientVerity);
        System.out.println("客户端解密:" + new String(clientDec));

        //------------------客户端发送
        byte[] clientData = "test客户端测试1234".getBytes();
        String clientSign = clientCipher.sign(clientData);
        System.out.println("客户端签名:");
        System.out.println(clientSign);
        byte[] clientEncData = clientCipher.encrypt(clientData);
        System.out.println("客户端加密:");
        System.out.println(Base64.encodeBase64String(clientEncData));
        //------------------服务端接收

        byte[] serverDec = serverCipher.decrypt(clientEncData);
        boolean serverVerity = serverCipher.verify(clientSign, serverDec);
        System.out.println("服务端验签:" + serverVerity);
        System.out.println("服务端解密:" + new String(serverDec));
    }
}