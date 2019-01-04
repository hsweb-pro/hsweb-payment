package org.hswebframework.payment.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * 资金账户类型
 * @author Lind
 * @since 1.0
 */
@Getter
@AllArgsConstructor
@Dict(id = "account-type")
public enum AccountType implements EnumDict<String> {

    NORMAL("NORMAL", "标准账户"),

    CREDIT("CREDIT", "信用账户"),

    MASTER("MASTER","归集账户"),

    DEPOSIT("DEPOSIT", "保证金账户");

    private String value;

    private String text;

}


