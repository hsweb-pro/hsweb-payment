package org.hswebframework.payment.account.service.impl;

import org.hswebframework.payment.account.dao.entity.AccountEntity;
import org.hswebframework.payment.account.dao.entity.AccountTransLogEntity;
import org.hswebframework.payment.account.service.LocalAccountService;
import org.hswebframework.payment.account.service.LocalAccountTransLogService;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.payment.api.account.AccountTransLog;
import org.hswebframework.payment.api.account.AccountTransService;
import org.hswebframework.payment.api.account.reqeust.AccountTransLogRequest;
import org.hswebframework.payment.api.account.reqeust.AccountTransRequest;
import org.hswebframework.payment.api.account.reqeust.QueryMerchantTransLogRequest;
import org.hswebframework.payment.api.account.response.AccountTransLogResponse;
import org.hswebframework.payment.api.account.response.AccountTransResponse;
import org.hswebframework.payment.api.enums.AccountTransType;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.web.bean.FastBeanCopier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lind
 * @since 1.0
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class AccountTransServiceImpl implements AccountTransService {

    @Autowired
    private LocalAccountService localAccountService;

    @Autowired
    private LocalAccountTransLogService localAccountTransLogService;

    @Override
    public AccountTransResponse deposit(AccountTransRequest request) {
        //查询账户信息
        AccountEntity account = localAccountService.queryAccountByAccountNo(request.getAccountNo());
        if (null == account) {
            throw ErrorCode.ACCOUNT_NOT_FOUND.createException();
        }
        if (StringUtils.isEmpty(request.getAccountName())) {
            request.setAccountName(account.getName());
        }
        long balance = account.getBalance() + request.getTransAmount();
        //更新资金账户信息
        localAccountService.deposit(account.getId(), request.getTransAmount());

        //保存交易日志
        localAccountTransLogService.insertTransLog(request, balance, AccountTransType.IN);

        AccountTransResponse response = new AccountTransResponse();
        response.setRequestId(request.getRequestId());
        response.setSuccess(true);
        response.setAccountNo(account.getAccountNo());
        response.setBalance(balance);
        response.setCompleteTime(new Date());// TODO: 2018/11/18 交易完成时间
        response.setTransType(request.getTransType());
        response.setCurrency(request.getCurrency());
        return response;
    }

    @Override
    public AccountTransResponse withdraw(AccountTransRequest request) {
        //查询资金账户
        AccountEntity account = localAccountService.queryAccountByAccountNo(request.getAccountNo());
        AccountTransResponse response = FastBeanCopier.copy(request, new AccountTransResponse());

        if (null == account) {
            throw ErrorCode.ACCOUNT_NOT_FOUND.createException();
        }
        if (StringUtils.isEmpty(request.getAccountName())) {
            request.setAccountName(account.getName());
        }
        //计算余额是否足够扣除
        long balance = account.getBalance() - request.getTransAmount();
        if (balance < 0) {
            response.setError(ErrorCode.INSUFFICIENT_BALANCE);
            return response;
        }
        //更新资金账户信息
        localAccountService.withdraw(account.getId(), request.getTransAmount());

        //保存交易日志
        localAccountTransLogService.insertTransLog(request, balance, AccountTransType.OUT);
        response.setSuccess(true);
        response.setAccountNo(account.getAccountNo());
        response.setBalance(balance);
        response.setCompleteTime(new Date());// TODO: 2018/11/18 交易完成时间
        response.setTransType(request.getTransType());
        response.setCurrency(request.getCurrency());
        return response;
    }

    @Override
    public AccountTransLogResponse queryTransLog(AccountTransLogRequest request) {
        List<AccountTransLogEntity> accountTransLogList = localAccountTransLogService.queryTransLogList(request.getMerchantId(), request.getTransType());
        List<AccountTransLog> transLogList = new ArrayList<>();
        AccountTransLogResponse response = new AccountTransLogResponse();
        accountTransLogList.forEach(logEntity -> {
                    AccountTransLog copy = FastBeanCopier.copy(logEntity, new AccountTransLog());
                    //为了兼容layUI
                    copy.setTransType(logEntity.getTransType().getText());
                    transLogList.add(copy);
                }
        );
        response.setTransLogList(transLogList);
        response.setSuccess(true);
        return response;
    }

    @Override
    public AccountTransLogResponse queryMerchantTransLog(QueryMerchantTransLogRequest request) {
        List<AccountTransLogEntity> transLogList = localAccountTransLogService.queryMerchantTransLog(request.getMerchantId(), request.getPaymentId());
        List<AccountTransLog> collect = transLogList
                .stream()
                .map(e -> {
                    AccountTransLog copy = FastBeanCopier.copy(e, new AccountTransLog());
                    copy.setTransType(e.getTransType().getText());
                    return copy;
                }).collect(Collectors.toList());
        AccountTransLogResponse response = new AccountTransLogResponse();
        response.setSuccess(true);
        response.setTransLogList(collect);
        return response;
    }
}
