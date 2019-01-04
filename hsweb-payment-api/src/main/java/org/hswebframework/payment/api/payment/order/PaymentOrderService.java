package org.hswebframework.payment.api.payment.order;

import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;

/**
 * 支付订单服务
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface PaymentOrderService {

    /**
     * 根据订单id获取订单详情
     *
     * @param orderId 订单id
     * @return 订单详情, 订单不存在则返回<code>null</code>
     */
    PaymentOrder getOrderById(String orderId);

    /**
     * 分页动态查询订单信息
     *
     * @param entity 动态查询实体
     * @return 分页查询结果
     */
    PagerResult<PaymentOrder> query(QueryParamEntity entity);

}
