package org.hswebframework.payment.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@Dict(id = "bind-card-purpose")
public enum BindCardPurpose implements EnumDict<String> {
    QUICK_PAY("快捷支付绑卡"),
    SETTLE("结算绑卡");

    private String text;

    @Override
    public String getValue() {
        return name();
    }
}
