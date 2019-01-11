package org.hswebframework.payment.api.account.reqeust;

import lombok.EqualsAndHashCode;
import org.hswebframework.payment.api.ApiRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lind
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AccountQueryRequest extends ApiRequest {

    private String accountNo;

    private String merchantId;

    public static AccountQueryRequest withAccountNo(String accountNo) {
        AccountQueryRequest request = new AccountQueryRequest();
        request.setAccountNo(accountNo);
        return request;
    }

    public static AccountQueryRequest withMerchantId(String merchantId) {
        AccountQueryRequest request = new AccountQueryRequest();
        request.setMerchantId(merchantId);
        return request;
    }
}
