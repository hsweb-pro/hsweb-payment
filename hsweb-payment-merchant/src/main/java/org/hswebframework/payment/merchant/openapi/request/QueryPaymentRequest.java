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
public class QueryPaymentRequest {

    @NotBlank(message = "商户ID[merchantId]不能为空")
    private String merchantId;

    private String orderId;

    private String paymentId;
}
