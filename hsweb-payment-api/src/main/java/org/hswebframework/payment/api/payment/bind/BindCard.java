package org.hswebframework.payment.api.payment.bind;

import org.hswebframework.payment.api.enums.BankCode;
import org.hswebframework.payment.api.enums.BindCardStatus;
import org.hswebframework.payment.api.enums.BindCardPurpose;
import org.hswebframework.payment.api.enums.IdType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class BindCard {

    private String id;

    //绑卡用途
    private BindCardPurpose purpose;

    private String channel;

    private String channelId;

    private String channelName;

    private String merchantId;

    private BankCode bankCode;

    private String accountName;

    private String accountNo;

    private IdType idType;

    private String idNumber;

    private String phoneNumber;

    private String cardType;

    private String validDate;

    private String cvn2;

    private BindCardStatus status;

    //绑卡授权码
    private String authorizeCode;

    //绑卡确认码
    private String bindConfirmCode;

    private Date createTime;

    private Date completeTime;
}
