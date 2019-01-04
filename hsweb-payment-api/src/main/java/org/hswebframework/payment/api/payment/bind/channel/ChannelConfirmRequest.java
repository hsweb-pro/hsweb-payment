package org.hswebframework.payment.api.payment.bind.channel;

import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.payment.bind.BindCard;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelConfirmRequest extends ApiRequest {
    /**
     * 待绑定的信息
     */
    private BindCard bindingCard;

    /**
     * 短信验证码
     */
    private String smsCode;
}
