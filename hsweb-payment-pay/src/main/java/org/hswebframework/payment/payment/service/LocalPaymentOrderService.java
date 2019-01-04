package org.hswebframework.payment.payment.service;

import org.hswebframework.payment.payment.entity.PaymentOrderEntity;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.service.*;

import java.util.Date;
import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface LocalPaymentOrderService extends
        InsertService<PaymentOrderEntity, String>,
        CreateEntityService<PaymentOrderEntity>,
        QueryByEntityService<PaymentOrderEntity>,
        QueryService<PaymentOrderEntity, String>,
        UpdateService<PaymentOrderEntity, String> {

    List<PaymentOrderEntity> queryPayingOrder(Date createTimeLt,int pageSize);

    int updateTimeoutStatus(List<String> paymentIdList);

    /**
     * 动态查询商户订单
     * @param entity
     * @return
     */
    PagerResult<PaymentOrderEntity> queryMerchantOrder(QueryParamEntity entity);

    /**
     * 商户根据ID查询order
     * @param merchantId
     * @param orderId
     * @return
     */
    PaymentOrderEntity queryOrderByMerchantIdAndOrderId(String merchantId,String orderId);

    /**
     * 代理商查询商户的订单
     * @param agentId
     * @param merchantId
     * @return
     */
    List<PaymentOrderEntity> queryAgentSingleMerchantOrder(String agentId,String merchantId);


    PagerResult<PaymentOrderEntity> queryAgentAllMerchantOrder(String agentId, QueryParamEntity paramEntity);


    PaymentOrderEntity queryOrderByIdAndMerchantId(String merchantId,String orderId);
}
