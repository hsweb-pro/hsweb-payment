package org.hswebframework.payment.account.service;

import org.hswebframework.payment.account.dao.entity.AccountTransLogEntity;
import org.hswebframework.payment.api.account.reqeust.AccountTransRequest;
import org.hswebframework.payment.api.enums.AccountTransType;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 * @author Lind
 * @since 1.0
 */
public interface LocalAccountTransLogService extends CrudService<AccountTransLogEntity, String> {

    /**
     * 保存交易日志
     * @param request
     * @return
     */
    void insertTransLog(AccountTransRequest request, Long currentBalance, AccountTransType accountTransType);


    /**
     * 查询交易记录
     * @param merchantId 商户ID
     * @param transType 交易类型
     * @return 交易记录
     */
    List<AccountTransLogEntity> queryTransLogList(String merchantId, AccountTransType transType);


    /**
     * 查询商户订单
     * @param merchantId 商户ID
     * @param paymentId 订单ID
     * @return
     */
    List<AccountTransLogEntity> queryMerchantTransLog(String merchantId,String paymentId);
}
