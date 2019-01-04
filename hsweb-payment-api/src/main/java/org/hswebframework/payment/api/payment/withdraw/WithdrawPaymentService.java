package org.hswebframework.payment.api.payment.withdraw;


import org.hswebframework.payment.api.enums.TransType;

/**
 * @author Lind
 * @since 1.0
 * @see TransType#WITHDRAW
 * @see WithdrawPaymentChannel
 */
public interface WithdrawPaymentService {

    /**
     * 发起提现请求
     *
     * @param request 请求对象
     * @return 发起代付请求结果
     */
    WithdrawPaymentResponse requestWithdrawPayment(WithdrawPaymentRequest request);

}
