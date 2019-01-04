package org.hswebframework.payment.payment.entity;

import org.hswebframework.payment.api.enums.NotifyType;
import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.enums.TransType;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Table
@Getter
@Setter
public class NotificationLogEntity extends SimpleGenericEntity<String> {

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "channel")
    private String channel;

    @Column(name = "trans_type")
    private TransType transType;

    @Column(name = "notify_type")
    private NotifyType notifyType;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "complete_time")
    private Date completeTime;

    @Column(name = "notify_config")
    private Map<String, Object> notifyConfig;

    @Column(name = "extra_param")
    private Map<String, String> extraParam;

    @Column(name = "retry_times")
    private Integer retryTimes;

    @Column(name = "last_notify_time")
    private Date lastNotifyTime;

    @Column(name = "is_notify_success")
    private Boolean notifySuccess;

    @Column(name = "error_reason")
    private String errorReason;
}
