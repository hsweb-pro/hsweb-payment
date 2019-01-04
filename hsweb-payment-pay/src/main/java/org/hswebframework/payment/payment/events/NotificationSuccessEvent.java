package org.hswebframework.payment.payment.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public class NotificationSuccessEvent {
    String paymentId;
}
