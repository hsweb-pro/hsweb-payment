package org.hswebframework.payment.api.account;

import org.hswebframework.payment.api.enums.CurrencyEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class AccountTransLog implements Serializable {


    private String paymentId;

    private String accountNo;

    private String merchantId;

    private CurrencyEnum currency;

    private String transType;

    private Long transAmount;

    private Long balance;

    private boolean status;

    private String comment;

    private String id;

    private Date createTime;

    private String createUser;

}
