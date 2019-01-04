package org.hswebframework.payment.account.service.impl;

import org.hswebframework.payment.account.dao.AccountTransLogDao;
import org.hswebframework.payment.account.dao.entity.AccountTransLogEntity;
import org.hswebframework.payment.account.service.LocalAccountTransLogService;
import org.hswebframework.payment.api.account.reqeust.AccountTransRequest;
import org.hswebframework.payment.api.enums.AccountTransType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Lind
 * @since 1.0
 */
@Service
public class LocalAccountTransLogServiceImpl extends GenericEntityService<AccountTransLogEntity, String> implements LocalAccountTransLogService {

    @Autowired
    private AccountTransLogDao accountTransLogDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public CrudDao<AccountTransLogEntity, String> getDao() {
        return accountTransLogDao;
    }

    @Override
    public void insertTransLog(AccountTransRequest request,
                               Long currentBalance,
                               AccountTransType transType) {
        //加资金流水
        AccountTransLogEntity log = AccountTransLogEntity
                .builder()
                .id(getIDGenerator().generate())
                .paymentId(request.getPaymentId())
                .accountNo(request.getAccountNo())
                .accountName(request.getAccountName())
                .transAmount(request.getTransAmount())
                .currency(request.getCurrency())
                .transType(request.getTransType())
                .balance(currentBalance)
                .accountTransType(transType)
                .merchantId(request.getMerchantId())
                .paymentId(request.getPaymentId())
                .createTime(new Date())
                .comment(request.getComment())
                .build();
        log.initData("Lind");
        accountTransLogDao.insert(log);
    }

    @Override
    public List<AccountTransLogEntity> queryTransLogList(String merchantId, AccountTransType transType) {
        if (transType.eq(AccountTransType.IN)) {
            return createQuery()
                    .where("merchantId", merchantId)
                    .and("transType", TransType.WITHDRAW)
                    .listNoPaging();
        } else if (transType.eq(AccountTransType.OUT)) {
            return createQuery()
                    .where("merchantId", merchantId)
                    .not("transType", TransType.WITHDRAW)
                    .listNoPaging();
        } else {
            return new ArrayList<>();
        }

    }

    @Override
    public List<AccountTransLogEntity> queryMerchantTransLog(String merchantId, String paymentId) {
        return createQuery().where("merchantId",merchantId).and("paymentId",paymentId).listNoPaging();
    }


}
