package org.hswebframework.payment.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
@Dict(id = "merchant-status")
public enum MerchantStatus implements EnumDict<String> {

    PENDING_REVIEW("待审核"),
    REVIEW_COMPLETED("审核完成"),
    ACTIVE("正常"),
    FREEZE("冻结");
    private String text;

    @Override
    public String getValue() {
        return name();
    }
}
