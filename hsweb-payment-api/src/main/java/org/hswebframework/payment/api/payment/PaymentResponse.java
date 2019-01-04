package org.hswebframework.payment.api.payment;

import org.hswebframework.payment.api.ApiResponse;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class PaymentResponse extends ApiResponse {

    /**
     * 支付ID
     */
    private String paymentId;

    /**
     * 商户订单号
     */
    private String orderId;

    /**
     * 支付渠道
     */
    private String channel;

    /**
     * 支付渠道ID
     */
    private String channelId;

}
