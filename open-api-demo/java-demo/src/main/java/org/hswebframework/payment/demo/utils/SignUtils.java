package org.hswebframework.payment.demo.utils;

import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class SignUtils {

    public static String sign(Map<String, Object> param, String key) {
        // key=value&key2=value2&key
        return DigestUtils.md5Hex(objectToUrlParameters(param) + "&" + key);
    }

    public static boolean verifySign(Map<String, Object> param, String key) {
        String sign = (String) param.remove("sign");
        return sign(param, key).equalsIgnoreCase(sign);
    }

    public static String objectToUrlParameters(Map<String, Object> target) {
        return new TreeMap<>(target).entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + encodeParameter(entry.getValue()))
                .reduce((s1, s2) -> s1.concat("&").concat(s2))
                .orElse("");
    }

    @SneakyThrows
    public static String encodeParameter(Object param) {
        return String.valueOf(param);
//        return URLEncoder.encode(String.valueOf(param), "utf-8");
    }
}
