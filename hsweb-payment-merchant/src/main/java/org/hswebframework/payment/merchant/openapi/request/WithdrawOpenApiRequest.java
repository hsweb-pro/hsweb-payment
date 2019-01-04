package org.hswebframework.payment.merchant.openapi.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;


@Getter
@Setter
public class WithdrawOpenApiRequest {

    @NotBlank(message = "提现金额不能为空")
    private Long amount;

    @NotBlank(message = "商户ID不能为空")
    private String merchantId;

    @NotBlank
    private String ipAddress;

}
