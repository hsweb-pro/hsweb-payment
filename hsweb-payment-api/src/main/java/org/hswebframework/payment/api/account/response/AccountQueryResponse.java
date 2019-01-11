package org.hswebframework.payment.api.account.response;

import lombok.EqualsAndHashCode;
import org.hswebframework.payment.api.ApiResponse;
import org.hswebframework.payment.api.account.Account;
import lombok.Data;

/**
 * @author Lind
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountQueryResponse extends ApiResponse {

    private Account account;
}
