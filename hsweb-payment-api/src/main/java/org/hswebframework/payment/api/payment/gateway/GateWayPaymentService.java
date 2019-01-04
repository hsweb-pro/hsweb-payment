package org.hswebframework.payment.api.payment.gateway;

import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.exception.PaymentException;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.PaymentResponse;
import org.hswebframework.web.validate.ValidationException;

/**
 * @author zhouhao
 * @see TransType#GATEWAY
 * @since 1.0.0
 */
public interface GateWayPaymentService {
    /**
     * 发起网关支付请求
     *
     * @param request 请求参数
     * @return 支付请求结果, 具体的结果根据渠道不同返回不同的类型, 具体参照对应的渠道文档
     * @throws PaymentException    发起支付过程失败
     * @throws ValidationException 请求参数校验失败
     */
    PaymentResponse requestGateWayPayment(PaymentRequest request) throws PaymentException, ValidationException;

}
