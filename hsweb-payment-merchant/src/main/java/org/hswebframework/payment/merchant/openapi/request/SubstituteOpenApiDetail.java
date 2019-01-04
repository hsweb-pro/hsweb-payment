package org.hswebframework.payment.merchant.openapi.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class SubstituteOpenApiDetail {
    @NotBlank(message = "明细交易流水号[transNo]不能为空")
    private String transNo;

    @Range(min = 100, message = "单笔代付不能小于1元")
    private long amount;

    private String remark;
}
