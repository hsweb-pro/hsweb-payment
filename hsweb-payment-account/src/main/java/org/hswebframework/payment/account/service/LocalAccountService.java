package org.hswebframework.payment.account.service;

import org.hswebframework.payment.account.dao.entity.AccountEntity;
import org.hswebframework.web.service.CrudService;

/**
 * @author Lind
 * @since 1.0
 */
public interface LocalAccountService extends CrudService<AccountEntity,String> {


    boolean deposit(String id,long amount);

    boolean withdraw(String id,long amount);


    /**
     * 根据资金账户号查询资金账户
     * @param accountNo
     * @return
     */
    AccountEntity queryAccountByAccountNo(String accountNo);

    Long queryBalanceByMerchantId(String merchantId);

    void addFreezeBalance(String accountNo,Long amount);

    void subtractFreezeBalance(String accountNo,Long amount);
}
