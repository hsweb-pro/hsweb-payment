package org.hswebframework.payment.payment.channel.alipay;

import org.springframework.stereotype.Component;

/**
 * 支付宝电脑网站
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Component
public class OfficialAlipayWebChannel extends AbstractOfficialAlipayChannel {
    @Override
    protected RequestType getType() {
        return RequestType.WEB;
    }

    @Override
    public String getChannel() {
        return "alipay-web";
    }

    @Override
    public String getChannelName() {
        return "网页-支付宝";
    }
}
