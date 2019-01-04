package org.hswebframework.payment.payment.notify;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 渠道的支付通知
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ChannelNotification {

    private Map<String, Object> parameters;

    public static ChannelNotification of(Map<String, Object> parameters) {
        ChannelNotification notification = new ChannelNotification();
        notification.parameters = parameters;
        return notification;
    }
}
