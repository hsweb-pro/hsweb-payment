package org.hswebframework.payment.payment.entity;

import org.hswebframework.payment.api.enums.BankCode;
import org.hswebframework.payment.api.enums.BindCardPurpose;
import org.hswebframework.payment.api.enums.BindCardStatus;
import org.hswebframework.payment.api.enums.IdType;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Table
@Getter
@Setter
public class BindCardEntity extends SimpleGenericEntity<String> {
    //绑卡用途
    @Column(name = "purpose")
    private BindCardPurpose purpose;

    @Column(name = "channel")
    private String channel;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "bank_code")
    private BankCode bankCode;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "id_type")
    private IdType idType;
    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "card_type")
    private String cardType;

    @Column(name = "valid_date")
    private String validDate;

    @Column(name = "cvn2")
    private String cvn2;

    @Column(name = "status")
    private BindCardStatus status;

    //绑卡授权码
    @Column(name = "authorize_code")
    private String authorizeCode;
    //绑卡确认码
    @Column(name = "bind_confirm_code")
    private String bindConfirmCode;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "complete_time")
    private Date completeTime;

    @Column(name = "memo")
    private String memo;

    public void requestComplete(String bindConfirmCode) {
        setBindConfirmCode(bindConfirmCode);
        setStatus(BindCardStatus.binding);
    }

    public void confirmComplete(boolean success, String authorizeCode) {
        setCompleteTime(new Date());
        setStatus(success ? BindCardStatus.success : BindCardStatus.failed);
        setAuthorizeCode(authorizeCode);
    }
}
