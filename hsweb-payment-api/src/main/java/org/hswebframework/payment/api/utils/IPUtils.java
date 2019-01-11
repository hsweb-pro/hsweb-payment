package org.hswebframework.payment.api.utils;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class IPUtils {

    public static String getRealIp(String ip) {
        String ids[] = ip.split("[,]");
        return ids[ids.length - 1].trim();
    }
}
