package org.hswebframework.payment.payment.notify;


import org.hswebframework.payment.api.enums.NotifyType;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface NotifierProvider {
    NotifyType getType();

    Notifier createNotifier(Notification notification);
}
