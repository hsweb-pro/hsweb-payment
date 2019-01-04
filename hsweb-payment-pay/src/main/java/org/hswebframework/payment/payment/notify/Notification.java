package org.hswebframework.payment.payment.notify;

import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.enums.TransType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class Notification implements Serializable {

    private String paymentId;

    private TransType transType;

    private String channel;

    private String channelOrderId;

    private String orderId;

    private String merchantId;

    private String productId;

    private long amount;

    private String currency;

    private PaymentStatus status;

    private Date completeTime;

    private Map<String, Object> notifyConfig;

    private Map<String, String> extraParam;

}
