package org.hswebframework.payment.api.payment;

import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.api.payment.order.PaymentOrderService;

/**
 * 支持主动查询订单结果的渠道
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface ActiveQuerySupportPaymentChannel extends ChannelProvider {
    /**
     * @return 交易类型
     */
    TransType getTransType();

    /**
     * @return 渠道标识
     */
    String getChannel();

    /**
     * @return 渠道名称
     */
    String getChannelName();

    /**
     * 发起主动查询支付订单结果
     *
     * @param order 支付订单
     * @see PaymentOrder
     * @see PaymentOrderService
     */
    void doActiveQueryOrderResult(PaymentOrder order);
}
