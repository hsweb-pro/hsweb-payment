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
@Dict(id = "account-status")
public enum AccountStatus implements EnumDict<String> {

    ACTIVE("ACTIVE","激活"),

    FREEZE("FREEZE","冻结");

    private String value;

    private String text;

}


