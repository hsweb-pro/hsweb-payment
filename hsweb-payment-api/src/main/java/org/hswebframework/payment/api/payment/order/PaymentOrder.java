package org.hswebframework.payment.api.payment.order;

import org.hswebframework.payment.api.enums.CurrencyEnum;
import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.enums.TransType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class PaymentOrder {
    private String id;

    private TransType transType;

    private String channel;
    private String channelName;
    private String channelId;

    private String orderId;

    private String productId;

    private String merchantId;

    private String merchantName;

    private String productName;

    //订单总金额
    private long amount;
    //实际支付金额
    private long realAmount;

    private CurrencyEnum currency;

    private Date createTime;

    private Date updateTime;

    private Date completeTime;

    private String channelProvider;

    private String channelProviderName;

    private PaymentStatus status;

    private String comment;
}
