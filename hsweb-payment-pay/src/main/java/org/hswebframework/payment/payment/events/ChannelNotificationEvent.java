package org.hswebframework.payment.payment.events;

import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public class ChannelNotificationEvent {
    ChannelNotificationResult result;
}
