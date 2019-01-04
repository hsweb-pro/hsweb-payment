package org.hswebframework.payment.merchant.openapi.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class QueryPaymentResponse {

    private boolean success;

    private String paymentId;

    private String orderId;

    private String status;

    private String statusText;

    private long amount;

    private String completeTime;

    private String channel;

    private String channelName;
}
