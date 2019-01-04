package org.hswebframework.payment.payment.channel.alipay;

import org.hswebframework.payment.api.payment.PaymentResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class AlipayWebsiteResponse extends PaymentResponse {
    private String payUrl;
}
