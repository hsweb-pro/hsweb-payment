package org.hswebframework.payment.api.payment.events;

import org.hswebframework.payment.api.payment.order.PaymentOrder;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class WithHoldPaymentCompleteEvent extends PaymentCompleteEvent {
    public WithHoldPaymentCompleteEvent(PaymentOrder order) {
        super(order);
    }
}
