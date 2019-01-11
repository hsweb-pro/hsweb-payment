package org.hswebframework.payment.api.account.reqeust;

import lombok.EqualsAndHashCode;
import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.enums.AccountTransType;
import lombok.Data;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccountTransLogRequest extends ApiRequest {

    private String merchantId;

    private AccountTransType transType;
}
