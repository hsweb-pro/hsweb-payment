package org.hswebframework.payment.api.payment.quick.channel;

import org.hswebframework.payment.api.payment.PaymentRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ChannelQuickPaymentRequest extends PaymentRequest {
    //绑卡渠道ID
    private String channelId;

    private String bindId;

    //绑卡渠道授权吗
    private String channelAuthorizeCode;

    private String accountName;

    private String accountNumber;

    private String phoneNumber;

    private String idNumber;
//    //绑卡流水号
//    private String bindNo;

    private String validDate;

    private String cvn2;

    private String remarks;
}
