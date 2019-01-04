package org.hswebframework.payment.payment.notify;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class NotifyResult {
    private String errorReason;

    private boolean success;

    public static NotifyResult success() {
        NotifyResult result = new NotifyResult();
        result.setSuccess(true);
        return result;
    }

    public static NotifyResult error(String errorReason) {
        NotifyResult result = new NotifyResult();
        result.setSuccess(false);
        result.setErrorReason(errorReason);
        return result;
    }
}
