package org.hswebframework.payment.payment.channel.alipay;

import org.springframework.stereotype.Component;

/**
 * 支付宝H5
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Component
public class OfficialAlipayWapChannel extends AbstractOfficialAlipayChannel {
    @Override
    protected RequestType getType() {
        return RequestType.WAP;
    }

    @Override
    public String getChannel() {
        return "alipay-h5";
    }

    @Override
    public String getChannelName() {
        return "H5-支付宝";
    }
}
