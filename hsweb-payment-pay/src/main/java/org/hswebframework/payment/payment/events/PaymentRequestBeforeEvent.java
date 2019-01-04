package org.hswebframework.payment.payment.events;

import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.PaymentChannel;
import org.hswebframework.payment.api.payment.PaymentRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public class PaymentRequestBeforeEvent {
    private String paymentId;

    private TransType transType;

    private PaymentChannel paymentChannel;

    private PaymentRequest request;

}
