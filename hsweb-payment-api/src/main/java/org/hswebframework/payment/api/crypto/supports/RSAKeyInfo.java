package org.hswebframework.payment.api.crypto.supports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RSAKeyInfo {
    private String publicKey;

    private String privateKey;


    public String formatPublicKey() {
        return format(publicKey);
    }

    public String formatPrivateKey() {
        return format(privateKey);
    }

    private String format(String key) {
        StringBuilder builder = new StringBuilder();
        char[] chars = key.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != 0 && i % 64 == 0) {
                builder.append("\n");
            }
            builder.append(chars[i]);
        }

        return builder.toString();
    }
}
