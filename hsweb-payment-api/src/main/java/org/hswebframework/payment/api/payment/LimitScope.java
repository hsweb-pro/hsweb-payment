package org.hswebframework.payment.api.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.EnumDict;

/**
 * 限额范围
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum LimitScope implements EnumDict<String> {
    CHANNEL("渠道限额"),
    MERCHANT_CHANNEL("商户渠道限额"),
    MERCHANT_CHANNEL_SINGLE("商户单个渠道配置限额");

    private String text;

    @Override
    public String getValue() {
        return name();
    }
}
