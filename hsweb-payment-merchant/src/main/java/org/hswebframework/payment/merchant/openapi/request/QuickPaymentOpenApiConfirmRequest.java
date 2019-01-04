package org.hswebframework.payment.merchant.openapi.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class QuickPaymentOpenApiConfirmRequest {

    @NotBlank(message = "支付订单号[paymentId]不能为空")
    private String paymentId;

    @NotBlank(message = "短信验证码[smsCode]不能为空")
    private String smsCode;

}
