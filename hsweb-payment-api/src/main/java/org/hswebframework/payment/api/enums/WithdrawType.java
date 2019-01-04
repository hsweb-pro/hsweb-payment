package org.hswebframework.payment.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Dict(id = "withdraw-type")
public enum WithdrawType implements EnumDict<String>{

    AUTOMATIC("AUTOMATIC", "自动提现"),

    MANUAL("MANUAL", "手动提现");

    private String value;

    private String text;
}
