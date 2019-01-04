package org.hswebframework.payment.api.payment.events;

import org.hswebframework.payment.api.events.BusinessEvent;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付超时事件,当用户发起的支付请求超时后触发此事件.
 * 比如:用户发起了支付,但是未进行支付
 *
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public class PaymentTimeoutEvent implements BusinessEvent {
    private PaymentOrder order;
}
