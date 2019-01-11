package org.hswebframework.payment.test.channel;

import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.PaymentChannel;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.PaymentResponse;
import org.hswebframework.payment.payment.channel.AbstractPaymentChannel;
import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Component
@Profile({"dev", "test"})
public class TestGatewayPayChannel extends AbstractPaymentChannel<TestChannelConfig, PaymentRequest, PaymentResponse>
        implements PaymentChannel<PaymentRequest, PaymentResponse> {
    @Override
    protected PaymentResponse doRequestPay(TestChannelConfig config, PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);
        runLater(() -> {
            Random random = new Random();

            boolean success = random.nextInt(100) > config.getFailureRate();

            afterHandleChannelNotify(ChannelNotificationResult.builder()
                    .success(success)
                    .amount(request.getAmount())
                    .paymentId(request.getPaymentId())
                    .memo("测试")
                    .build());
        }, 1, TimeUnit.SECONDS);
        return response;
    }

    @Override
    public TransType getTransType() {
        return TransType.GATEWAY;
    }

    @Override
    public String getChannel() {
        return "alipay-web";
    }

    @Override
    public String getChannelName() {
        return "网页-支付宝";
    }

    @Override
    public String getChannelProvider() {
        return "test";
    }

    @Override
    public String getChannelProviderName() {
        return "测试环境";
    }
}
