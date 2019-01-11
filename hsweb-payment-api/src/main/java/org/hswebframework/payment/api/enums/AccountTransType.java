package org.hswebframework.payment.api.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

@Getter
@AllArgsConstructor
@Dict(id = "account-trans-type")
public enum AccountTransType implements EnumDict<String>{

    IN("IN", "入账"),

    OUT("OUT", "出账");

    private String value;

    private String text;
}
