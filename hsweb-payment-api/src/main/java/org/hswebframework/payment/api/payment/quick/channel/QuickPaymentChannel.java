package org.hswebframework.payment.api.payment.quick.channel;

import org.hswebframework.payment.api.enums.BindCardPurpose;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.PaymentChannel;
import org.hswebframework.payment.api.payment.bind.channel.BindCardChannel;

/**
 * 快捷支付渠道
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface QuickPaymentChannel<RES extends ChannelQuickPaymentResponse>
        extends PaymentChannel<ChannelQuickPaymentRequest, RES>, BindCardChannel {

    @Override
    default BindCardPurpose getPurpose() {
        return BindCardPurpose.QUICK_PAY;
    }

    @Override
    default TransType getTransType() {
        return TransType.QUICK;
    }

    /**
     * 确认支付
     *
     * @param confirmRequest 确认支付请求
     * @return 确认支付结果
     */
    ChannelQuickPaymentConfirmResponse confirmQuickPay(ChannelQuickPaymentConfirmRequest confirmRequest);
}
