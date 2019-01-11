package org.hswebframework.payment.api.account.response;

import lombok.EqualsAndHashCode;
import org.hswebframework.payment.api.ApiResponse;
import org.hswebframework.payment.api.enums.CurrencyEnum;
import org.hswebframework.payment.api.enums.TransType;
import lombok.Data;

import java.util.Date;

/**
 * @author Lind
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountTransResponse extends ApiResponse {

    private String orderNo;

    private String accountNo;

    private CurrencyEnum currency;

    private TransType transType;

    private Long transAmount;

    private Long balance;

    private String comment;

    private Date completeTime;
}
