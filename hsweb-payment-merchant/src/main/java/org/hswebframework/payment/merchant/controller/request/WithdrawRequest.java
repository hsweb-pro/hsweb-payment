package org.hswebframework.payment.merchant.controller.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
public class WithdrawRequest {

    @Range(min = 1, message = "提现金额不能小于0.01")
    private long transAmount;

    @NotBlank(message = "收款人不能为空")
    private String payeeId;

}
