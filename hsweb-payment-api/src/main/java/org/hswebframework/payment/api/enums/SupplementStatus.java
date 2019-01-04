package org.hswebframework.payment.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@Dict(id = "supplement-status")
public enum SupplementStatus implements EnumDict<String> {
    REQUEST("等待完成"),
    SUCCESS("已补登"),
    FAILED("补登失败"),
    ROLLBACK("已回退"),
    ;
    private String text;

    @Override
    public String getValue() {
        return name();
    }
}
