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
@Dict(id = "withdraw-status")
public enum WithdrawStatus implements EnumDict<String> {
    APPLYING("APPLYING","申请中"),
    HANDING("HANDING", "处理中"),
    FAILURE("FAILURE", "处理失败"),
    CLOSE("CLOSE","已关闭"),
    SUCCESS("SUCCESS", "处理成功");

    private String value;

    private String text;

}

