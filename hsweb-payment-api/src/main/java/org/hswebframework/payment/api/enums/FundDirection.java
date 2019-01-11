package org.hswebframework.payment.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

@AllArgsConstructor
@Getter
@Dict(id = "settle-fund-type")
public enum FundDirection implements EnumDict<String> {
    IN("收入"), OUT("支出");

    private String text;

    @Override
    public String getValue() {
        return name();
    }
}
