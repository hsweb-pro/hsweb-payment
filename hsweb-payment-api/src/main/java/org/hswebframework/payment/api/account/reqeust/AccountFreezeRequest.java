package org.hswebframework.payment.api.account.reqeust;

import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.enums.TransType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

@Getter
@Setter
public class AccountFreezeRequest extends ApiRequest {

    @NotBlank(message = "资金账户号不能为空")
    private String accountNo;

    @NotBlank(message = "paymentId不能为空")
    private String paymentId;

    @NotBlank(message = "冻结金额不能为空")
    private Long amount;

    @NotBlank(message = "交易类型不能为空")
    private TransType transType;

    @NotBlank(message = "商户ID")
    private String merchantId;

    private String comment;
}
