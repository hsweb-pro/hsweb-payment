package org.hswebframework.payment.api.utils;

import lombok.SneakyThrows;
import org.hswebframework.web.bean.FastBeanCopier;

import java.net.URLEncoder;
import java.util.TreeMap;
import java.util.function.Predicate;

public class UrlUtils {
    public static String objectToUrlParameters(Object target) {

        return objectToUrlParameters(target, key -> true);
    }

    public static String objectToUrlParameters(Object target, Predicate<String> keyFilter) {
        return FastBeanCopier.copy(target, TreeMap::new)
                .entrySet()
                .stream()
                .filter(entry -> keyFilter.test(String.valueOf(entry.getKey())))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((s1, s2) -> s1.concat("&").concat(s2))
                .orElse("");
    }

    @SneakyThrows
    public static String encodeParameter(Object param) {
        return URLEncoder.encode(String.valueOf(param), "utf-8");
    }
}
