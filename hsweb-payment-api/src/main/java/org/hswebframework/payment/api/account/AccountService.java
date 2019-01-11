package org.hswebframework.payment.api.account;

import org.hswebframework.payment.api.account.reqeust.AccountCreateRequest;
import org.hswebframework.payment.api.account.reqeust.AccountQueryRequest;
import org.hswebframework.payment.api.account.response.AccountCreateResponse;
import org.hswebframework.payment.api.account.response.AccountQueryResponse;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface AccountService {


    String MASTER_ACCOUNT_NO = "10000000000001";

    /**
     * 开立资金账户
     *
     * @param request
     * @return
     */
    AccountCreateResponse createAccount(AccountCreateRequest request);


    /**
     * 查询资金账户
     *
     * @param request
     * @return
     */
    AccountQueryResponse queryAccount(AccountQueryRequest request);

}
