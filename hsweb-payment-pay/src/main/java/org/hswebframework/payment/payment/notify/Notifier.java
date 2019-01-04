package org.hswebframework.payment.payment.notify;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface Notifier {
    NotifyResult doNotify() throws Exception;

    void cleanup();
}
