package org.hswebframework.payment.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@AllArgsConstructor
@Dict(id = "merchant-type")
public enum MerchantType implements EnumDict<String> {

    NORMAL("NORMAL", "标准账户"),
    AGENT("AGENT", "代理商户");

    private String value;

    private String text;

}