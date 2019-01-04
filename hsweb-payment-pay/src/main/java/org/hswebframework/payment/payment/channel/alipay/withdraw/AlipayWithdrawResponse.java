package org.hswebframework.payment.payment.channel.alipay.withdraw;

import org.hswebframework.payment.api.payment.withdraw.WithdrawPaymentResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class AlipayWithdrawResponse extends WithdrawPaymentResponse {

    private String channelOrderId;
}
