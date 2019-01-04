package org.hswebframework.payment.api.payment.quick;


import org.hswebframework.payment.api.enums.TransType;

/**
 * @author zhouhao
 * @see TransType#GATEWAY
 * @since 1.0.0
 */

public interface QuickPaymentService {

    /**
     * 发起快捷支付请求
     *
     * @param request 请求对象
     * @return 发起支付请求结果
     */
    QuickPaymentResponse requestQuickPayment(QuickPaymentRequest request);

    /**
     * 确认快捷支付
     *
     * @param request 确认请求对象
     * @return 确认结果
     */
    QuickPaymentConfirmResponse confirmQuickPayment(QuickPaymentConfirmRequest request);

}
