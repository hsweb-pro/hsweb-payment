package org.hswebframework.payment.api.payment;

import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.gateway.GateWayPaymentService;
import org.hswebframework.payment.api.payment.quick.QuickPaymentService;
import org.hswebframework.payment.api.payment.substitute.SubstituteChannel;
import org.hswebframework.payment.api.payment.substitute.SubstitutePaymentService;
import org.hswebframework.payment.api.payment.withdraw.WithdrawPaymentService;

import java.util.List;

/**
 * 统一支付服务,对外提供统一的支付请求服务.
 *
 * @author zhouhao
 * @see PaymentChannel
 * @since 1.0.0
 */
public interface PaymentService {

    /**
     * 获取所有渠道
     *
     * @return 所有渠道
     */
    List<PaymentChannel> getAllChannel();

    /**
     * @return 网关支付服务
     * @see TransType#GATEWAY
     */
    GateWayPaymentService gateway();

    /**
     * @return 快捷支付
     * @see TransType#QUICK
     */
    QuickPaymentService quick();

    /**
     * @return 提现交易
     * @see TransType#WITHDRAW
     */
    WithdrawPaymentService withdraw();

    /**
     * @return 代付交易
     * @see TransType#SUBSTITUTE
     * @see SubstituteChannel
     */
    SubstitutePaymentService substitute();
}
