package org.hswebframework.payment.api.account.reqeust;

import org.hswebframework.payment.api.ApiRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUnfreezeRequest extends ApiRequest {

    private String accountNo;

    private String paymentId;

    //解冻原因
    private String unfreezeComment;

//    private Long amount;

}
