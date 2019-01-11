package org.hswebframework.payment.test.channel;

import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.payee.BankPayee;
import org.hswebframework.payment.api.payment.substitute.SubstituteChannel;
import org.hswebframework.payment.api.payment.substitute.SubstituteDetailCompleteEvent;
import org.hswebframework.payment.api.payment.substitute.request.SubstituteDetail;
import org.hswebframework.payment.api.payment.substitute.request.SubstituteRequest;
import org.hswebframework.payment.api.payment.substitute.response.SubstituteResponse;
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
public class TestSubstituteChannel extends AbstractPaymentChannel<TestChannelConfig, SubstituteRequest<BankPayee>, SubstituteResponse>
        implements SubstituteChannel<BankPayee, SubstituteResponse> {
    @Override
    public PayeeType getPayeeType() {
        return PayeeType.BANK;
    }

    @Override
    protected SubstituteResponse doRequestPay(TestChannelConfig config, SubstituteRequest<BankPayee> request) {
        runLater(() -> {
            long totalAmount = 0;
            Random random = new Random();

            for (SubstituteDetail<BankPayee> detail : request.getDetails()) {
                boolean success = random.nextInt(100) > config.getFailureRate();
                if (success) {
                    totalAmount += detail.getAmount();
                }
                eventPublisher.publishEvent(SubstituteDetailCompleteEvent.builder()
                        .amount(detail.getAmount())
                        .success(success)
                        .detailId(detail.getId())
                        .paymentId(request.getPaymentId())
                        .build());

            }
            afterHandleChannelNotify(ChannelNotificationResult.builder()
                    .paymentId(request.getPaymentId())
                    .success(totalAmount > 0)
                    .amount(totalAmount)
                    .build());
        }, 10, TimeUnit.SECONDS); //10秒后自动完成

        SubstituteResponse response = new SubstituteResponse();
        response.setSuccess(true);

        return response;
    }

    @Override
    public TransType getTransType() {
        return TransType.SUBSTITUTE;
    }

    @Override
    public String getChannel() {
        return "substitute";
    }

    @Override
    public String getChannelName() {
        return "网银代付";
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
