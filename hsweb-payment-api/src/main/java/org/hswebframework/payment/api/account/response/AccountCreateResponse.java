package org.hswebframework.payment.api.account.response;

import org.hswebframework.payment.api.ApiResponse;
import org.hswebframework.payment.api.account.Account;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class AccountCreateResponse extends ApiResponse {
    private Account account;
}
