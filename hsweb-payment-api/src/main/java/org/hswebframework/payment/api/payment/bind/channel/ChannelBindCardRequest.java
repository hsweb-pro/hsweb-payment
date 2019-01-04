package org.hswebframework.payment.api.payment.bind.channel;

import org.hswebframework.payment.api.enums.BankCode;
import org.hswebframework.payment.api.enums.IdType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelBindCardRequest {
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
}
