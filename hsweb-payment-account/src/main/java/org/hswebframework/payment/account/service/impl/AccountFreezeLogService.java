package org.hswebframework.payment.account.service.impl;

import org.hswebframework.payment.account.dao.AccountFreezeLogDao;
import org.hswebframework.payment.account.dao.entity.AccountEntity;
import org.hswebframework.payment.account.dao.entity.AccountFreezeLogEntity;
import org.hswebframework.payment.account.service.LocalAccountFreezeLogService;
import org.hswebframework.payment.account.service.LocalAccountService;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.payment.api.account.AccountFreezeService;
import org.hswebframework.payment.api.account.reqeust.AccountFreezeRequest;
import org.hswebframework.payment.api.account.reqeust.AccountUnfreezeRequest;
import org.hswebframework.payment.api.account.response.AccountFreezeResponse;
import org.hswebframework.payment.api.account.response.AccountUnfreezeResponse;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.FreezeDirection;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;

@Service
@Slf4j
public class AccountFreezeLogService extends GenericEntityService<AccountFreezeLogEntity, String> implements AccountFreezeService, LocalAccountFreezeLogService {

    @Autowired
    private AccountFreezeLogDao accountFreezeLogDao;

    @Autowired
    private LocalAccountService localAccountService;


    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public CrudDao<AccountFreezeLogEntity, String> getDao() {
        return accountFreezeLogDao;
    }

    @Override
    public AccountFreezeResponse freeze(AccountFreezeRequest request) {
        //查询资金账户
        AccountEntity account = localAccountService.queryAccountByAccountNo(request.getAccountNo());
        Assert.notNull(account, ErrorCode.ACCOUNT_NOT_FOUND.getText());

        if (account.getBalance()<request.getAmount()){
            throw ErrorCode.INSUFFICIENT_BALANCE.createException();
        }

        //资金账户增加冻结金额
        localAccountService.addFreezeBalance(request.getAccountNo(), request.getAmount());

        //创建资金冻结记录
        AccountFreezeLogEntity freezeLogEntity = AccountFreezeLogEntity
                .builder()
                .transType(request.getTransType())
                .accountName(account.getName())
                .merchantId(request.getMerchantId())
                .accountNo(request.getAccountNo())
                .amount(request.getAmount())
                .direction(FreezeDirection.FREEZE_DIRECTION)
                .paymentId(request.getPaymentId())
                .freezeTime(new Date())
                .comment(request.getComment())
                .build();
        insert(freezeLogEntity);

        AccountFreezeResponse response= new AccountFreezeResponse();
        response.setMessage("冻结成功!");
        response.setSuccess(true);
        return response;
    }

    @Override
    public AccountUnfreezeResponse unfreeze(AccountUnfreezeRequest request) {

        //查询冻结记录
        AccountFreezeLogEntity freezeLogEntity = createQuery()
                .where(AccountFreezeLogEntity::getPaymentId, request.getPaymentId())
                .and(AccountFreezeLogEntity::getAccountNo, request.getAccountNo())
                .single();

        if (freezeLogEntity==null){
            throw ErrorCode.FREEZE_ERROR.createException();
        }
        if (freezeLogEntity.getDirection().equals(FreezeDirection.UNFREEZE_DIRECTION)){
            throw ErrorCode.DUPLICATE_FREEZE.createException();
        }
        //修改资金账户
        localAccountService.subtractFreezeBalance(freezeLogEntity.getAccountNo(),freezeLogEntity.getAmount());

        //修改冻结记录
        createUpdate().where(AccountFreezeLogEntity::getId,freezeLogEntity.getId())
                .set(AccountFreezeLogEntity::getUnfreezeTime, new Date())
                .set(AccountFreezeLogEntity::getUnfreezeComment,request.getUnfreezeComment())
                .set(AccountFreezeLogEntity::getDirection,FreezeDirection.UNFREEZE_DIRECTION)
                .exec();

        AccountUnfreezeResponse response = new AccountUnfreezeResponse();
        response.setMessage("解冻成功");
        response.setSuccess(true);
        return response;
    }
}


