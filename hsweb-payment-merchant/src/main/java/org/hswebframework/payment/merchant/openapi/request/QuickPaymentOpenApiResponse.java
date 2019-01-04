package org.hswebframework.payment.merchant.openapi.request;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class QuickPaymentOpenApiResponse {

    private boolean success;

    /**
     * 支付ID
     */
    private String paymentId;

    /**
     * 商户订单号
     */
    private String orderId;


}
