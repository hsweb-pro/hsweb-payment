package org.hswebframework.payment.payment.events;

import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.PaymentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public class PaymentRequestAfterEvent {

    private String paymentId;

    private TransType transType;

    private PaymentRequest request;

    private PaymentResponse response;
}
