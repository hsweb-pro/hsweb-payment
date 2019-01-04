package org.hswebframework.payment.api.payment.quick.channel;

import org.hswebframework.payment.api.ApiRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ChannelQuickPaymentConfirmRequest extends ApiRequest {

//    private BindCard bindCard;

    private String paymentId;

    private String smsCode;

}
