package org.hswebframework.payment.api.merchant.request;

import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.enums.WithdrawType;
import org.hswebframework.payment.api.payment.payee.Payee;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplyWithdrawRequest extends ApiRequest {

    @Range(min = 1, message = "提现金额不能小于0.01")
    private long amount;

    @NotBlank(message = "商户ID不能为空")
    private String merchantId;

    @NotNull
    private Date applyTime;

    @NotNull
    private WithdrawType withdrawType;

    //收款人类型
    @NotNull(message = "收款人类型不能为空")
    private PayeeType payeeType;

    //收款人信息
    @NotNull(message = "收款人不能为空")
    private Payee payee;
}
