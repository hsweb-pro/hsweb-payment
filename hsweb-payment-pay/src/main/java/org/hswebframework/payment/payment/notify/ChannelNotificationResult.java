package org.hswebframework.payment.payment.notify;

import org.hswebframework.payment.api.enums.PaymentStatus;
import lombok.*;

import java.util.Map;

/**
 * 渠道的支付通知
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelNotificationResult {
    private String paymentId;

    private boolean success;

    private long amount;

    private String memo;

    private Object resultObject;

}
