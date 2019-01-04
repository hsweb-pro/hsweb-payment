package org.hswebframework.payment.api.payment.events;

import org.hswebframework.payment.api.events.BusinessEvent;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 支付完成事件,当支付完成后,不管是失败还是成功,都将推送此事件
 *
 * @author zhouhao
 * @see BusinessEvent
 * @see PaymentOrder
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
@Setter
public class PaymentCompleteEvent implements BusinessEvent {
    private PaymentOrder order;
}
