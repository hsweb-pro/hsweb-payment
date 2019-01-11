package org.hswebframework.payment.api.payment;

import org.hswebframework.payment.api.enums.TransType;

import java.util.List;
import java.util.Random;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RandomPaymentChannelSelector implements PaymentChannelSelector {

    public static PaymentChannelSelector INSTANCE = new RandomPaymentChannelSelector();

    @Override
    public <REQ extends PaymentRequest, RES extends PaymentResponse> PaymentChannel<REQ, RES> select(PaymentRequest request, TransType transType, List<PaymentChannel<REQ, RES>> allChannel) {
        if (allChannel.size() == 1) {
            return allChannel.get(0);
        }
        Random random = new Random();

        return allChannel.get(random.nextInt(allChannel.size()));
    }
}
