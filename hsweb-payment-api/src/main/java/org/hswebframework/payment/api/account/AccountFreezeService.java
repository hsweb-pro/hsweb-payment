package org.hswebframework.payment.api.account;

import org.hswebframework.payment.api.account.reqeust.AccountFreezeRequest;
import org.hswebframework.payment.api.account.reqeust.AccountUnfreezeRequest;
import org.hswebframework.payment.api.account.response.AccountFreezeResponse;
import org.hswebframework.payment.api.account.response.AccountUnfreezeResponse;

public interface AccountFreezeService {

    /**
     * 资金冻结服务
     *
     * @param request 资金冻结订单
     * @return 资金冻结结果
     */
    AccountFreezeResponse freeze(AccountFreezeRequest request);


    /**
     * 资金解冻服务
     *
     * @param request 资金解冻订单
     * @return 资金解冻结果
     */
    AccountUnfreezeResponse unfreeze(AccountUnfreezeRequest request);


}
