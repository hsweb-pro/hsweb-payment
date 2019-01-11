package org.hswebframework.payment.api.account;

import org.hswebframework.payment.api.account.reqeust.AccountTransLogRequest;
import org.hswebframework.payment.api.account.reqeust.AccountTransRequest;
import org.hswebframework.payment.api.account.reqeust.QueryMerchantTransLogRequest;
import org.hswebframework.payment.api.account.response.AccountTransLogResponse;
import org.hswebframework.payment.api.account.response.AccountTransResponse;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface AccountTransService {


    /**
     * 上账
     * @param request
     * @return
     */
    AccountTransResponse deposit(AccountTransRequest request);


    /**
     * 下账
     * @param request
     * @return
     */
    AccountTransResponse withdraw(AccountTransRequest request);


    /**
     * 查询交易记录
     * @param request
     * @return
     */
    AccountTransLogResponse queryTransLog(AccountTransLogRequest request);

    /**
     * 查询商户交易记录
     * @param request
     * @return
     */
    AccountTransLogResponse queryMerchantTransLog(QueryMerchantTransLogRequest request);
}
