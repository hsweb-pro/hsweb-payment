package org.hswebframework.payment.api.payment;

import org.hswebframework.payment.api.enums.TransType;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface PaymentChannelSelector {

    <REQ extends PaymentRequest, RES extends PaymentResponse> PaymentChannel<REQ, RES>
    select(PaymentRequest request, TransType transType, List<PaymentChannel<REQ, RES>> allChannel);
}
