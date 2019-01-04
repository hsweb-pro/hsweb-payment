package org.hswebframework.payment.api.payment.quick.channel;

import org.hswebframework.payment.api.payment.PaymentResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ChannelQuickPaymentResponse extends PaymentResponse {
    private String confirmId;

}
