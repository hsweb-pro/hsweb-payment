package org.hswebframework.payment.account.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.render.Sql;
import org.hswebframework.payment.account.dao.AccountDao;
import org.hswebframework.payment.account.dao.entity.AccountEntity;
import org.hswebframework.payment.account.service.LocalAccountService;
import org.hswebframework.payment.api.account.Account;
import org.hswebframework.payment.api.account.AccountService;
import org.hswebframework.payment.api.account.reqeust.AccountCreateRequest;
import org.hswebframework.payment.api.account.reqeust.AccountQueryRequest;
import org.hswebframework.payment.api.account.response.AccountCreateResponse;
import org.hswebframework.payment.api.account.response.AccountQueryResponse;
import org.hswebframework.payment.api.enums.AccountStatus;
import org.hswebframework.payment.api.enums.AccountType;
import org.hswebframework.payment.api.enums.CurrencyEnum;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Date;


/**
 * @author Lind
 * @since 1.0
 */
@Service
@Slf4j
public class AccountServiceImpl extends GenericEntityService<AccountEntity, String> implements AccountService, LocalAccountService, CommandLineRunner {

    @Autowired
    private AccountDao accountDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public AccountDao getDao() {
        return accountDao;
    }

    @Override
    public AccountCreateResponse createAccount(AccountCreateRequest request) {

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.initData(request.getCreateUser());
        accountEntity.setAccountNo(request.getAccountNo());
        accountEntity.setType(request.getAccountType());
        accountEntity.setStatus(AccountStatus.ACTIVE);
        accountEntity.setBalance(0L);
        accountEntity.setName(request.getName());
        accountEntity.setMerchantId(request.getMerchantId());
        accountEntity.setCurrency(request.getCurrency());
        accountEntity.setFreezeBalance(0L);
        String accountId = insert(accountEntity);

        AccountEntity account = selectByPk(accountId);

        Account copy = FastBeanCopier.copy(account, new Account());

        AccountCreateResponse response = new AccountCreateResponse();
        response.setSuccess(true);
        response.setMessage("开立资金账户成功");
        response.setCode("200");
        response.setAccount(copy);
        return response;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AccountQueryResponse queryAccount(AccountQueryRequest request) {
        AccountQueryResponse response = new AccountQueryResponse();
        response.setRequestId(request.getRequestId());

        Assert.isTrue(StringUtils.hasText(request.getAccountNo()) || StringUtils.hasText(request.getMerchantId()), "参数错误");

        AccountEntity accountEntity = createQuery()
                .where("merchantId", request.getMerchantId())
                .and("accountNo", request.getAccountNo())
                .single();

        if (accountEntity == null) {
            response.setSuccess(false);
            response.setCode(ErrorCode.ACCOUNT_NOT_FOUND.getValue());
            response.setMessage(ErrorCode.ACCOUNT_NOT_FOUND.getText());
        } else {
            Account account = FastBeanCopier.copy(accountEntity, new Account());
            response.setAccount(account);
            response.setSuccess(true);
        }
        return response;
    }

    @Override
    public boolean deposit(String id, long amount) {
        return createUpdate()
                .set(AccountEntity::getBalance, Sql.build("balance+" + amount))
                .set(AccountEntity::getUpdateTime, new Date())
                .where(AccountEntity::getId, id)
                .exec() > 0;
    }

    @Override
    public boolean withdraw(String id, long amount) {
        return createUpdate()
                .set(AccountEntity::getBalance, Sql.build("balance-" + amount))
                .set(AccountEntity::getUpdateTime, new Date())
                .where(AccountEntity::getId, id)
                .exec() > 0;
    }

    @Override
    public AccountEntity queryAccountByAccountNo(String accountNo) {
        return createQuery().where("accountNo", accountNo).forUpdate().single();
    }

    @Override
    public Long queryBalanceByMerchantId(String merchantId) {
        return createQuery().where("merchantId", merchantId).single().getBalance();
    }

    @Override
    public void addFreezeBalance(String accountNo, Long amount) {
        createUpdate()
                .where(AccountEntity::getAccountNo, accountNo)
                .set(AccountEntity::getBalance, Sql.build("balance-" + amount))
                .set(AccountEntity::getFreezeBalance, Sql.build("freeze_balance+" + amount)).exec();
    }

    @Override
    public void subtractFreezeBalance(String accountNo, Long amount) {
        createUpdate()
                .where(AccountEntity::getAccountNo, accountNo)
                .set(AccountEntity::getBalance, Sql.build("balance+" + amount))
                .set(AccountEntity::getFreezeBalance, Sql.build("freeze_balance-" + amount)).exec();
    }


    @Override
    public void run(String... strings) {
        int accountNo = createQuery().where("accountNo", MASTER_ACCOUNT_NO).total();
        if (accountNo < 1) {
            AccountCreateRequest request = AccountCreateRequest
                    .builder()
                    .accountNo(MASTER_ACCOUNT_NO)
                    .merchantId(MerchantService.MASTER_MERCHANT_ID)
                    .name("平台资金账户")
                    .createUser("system")
                    .currency(CurrencyEnum.CNY)
                    .accountType(AccountType.MASTER)
                    .build();
            AccountCreateResponse response = createAccount(request);
            response.assertSuccess();
            log.info("系统初始化:创建系统归集资金账户");
        }
    }
}
