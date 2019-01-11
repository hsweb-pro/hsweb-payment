package org.hswebframework.payment.payment.channel.alipay.substitute;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayFundTransToaccountTransferModel;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.ChannelProvider;
import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.payment.substitute.SubstituteChannel;
import org.hswebframework.payment.api.payment.substitute.SubstituteDetailCompleteEvent;
import org.hswebframework.payment.api.payment.substitute.request.SubstituteDetail;
import org.hswebframework.payment.api.payment.substitute.request.SubstituteRequest;
import org.hswebframework.payment.api.utils.Money;
import org.hswebframework.payment.payment.channel.AbstractPaymentChannel;
import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import org.hswebframework.web.bean.FastBeanCopier;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 支付宝批量代付渠道
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
@RestController
public class AlipaySubstituteSingleTransChannel extends AbstractPaymentChannel<AlipaySubstituteConfig, SubstituteRequest<Payee>, AlipaySubstituteResponse>
        implements SubstituteChannel<Payee, AlipaySubstituteResponse> {
    @Override
    public PayeeType getPayeeType() {
        return PayeeType.ALIPAY;
    }

    private AlipayClient createAlipayClient(AlipaySubstituteConfig config) {
        return new DefaultAlipayClient(
                config.getUrl(), config.getAppId(), config.getRsaPrivateKey(),
                "json", "utf-8", config.getPublicKey(), config.getSignType(),
                config.getProxyHost(),
                config.getProxyPort());
    }

    @Override
    public TransType getTransType() {
        return TransType.SUBSTITUTE;
    }

    @Override
    protected AlipaySubstituteResponse doRequestPay(AlipaySubstituteConfig config, SubstituteRequest<Payee> request) {
        AlipaySubstituteResponse response = FastBeanCopier.copy(request, new AlipaySubstituteResponse());

        AlipayClient client = createAlipayClient(config);
        try {

            String batchNo = request.getPaymentId();

            //异步转账
            runLater(() -> {
                try (MDC.MDCCloseable closeable = MDC.putCloseable("businessId", request.getPaymentId())) {
                    AtomicLong totalAmount = new AtomicLong();
                    //异步处理
                    for (SubstituteDetail<Payee> detail : request.getDetails()) {
                        try {
                            AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();
                            model.setPayeeAccount(detail.getPayee().getPayee());
                            model.setPayeeRealName(detail.getPayee().getPayeeName());
                            model.setPayeeType("ALIPAY_LOGONID");
                            model.setAmount(Money.cent(request.getAmount()).toString());
                            model.setRemark(request.getRemark());
                            model.setPayerShowName(config.getAccountName());
                            model.setOutBizNo(detail.getId());

                            AlipayFundTransToaccountTransferRequest transferRequest = new AlipayFundTransToaccountTransferRequest();
                            transferRequest.setBizModel(model);
                            AlipayFundTransToaccountTransferResponse channelResponse = client.execute(transferRequest);

                            //付款成功
                            if (channelResponse.isSuccess()) {
                                totalAmount.addAndGet(detail.getAmount());
                            }
                            eventPublisher.publishEvent(SubstituteDetailCompleteEvent.builder()
                                    .amount(detail.getAmount())
                                    .detailId(detail.getId())
                                    .paymentId(request.getPaymentId())
                                    .memo(channelResponse.getSubMsg())
                                    .success(channelResponse.isSuccess())
                                    .build());

                        } catch (Exception e) {
                            log.error("批量代付,发起转账到支付宝账户失败", e);
                            eventPublisher.publishEvent(SubstituteDetailCompleteEvent.builder()
                                    .amount(detail.getAmount())
                                    .detailId(detail.getId())
                                    .paymentId(request.getPaymentId())
                                    .memo(e.getMessage())
                                    .success(false)
                                    .build());
                        }
                    }
                    ChannelNotificationResult result = new ChannelNotificationResult();
                    result.setAmount(totalAmount.get());
                    result.setPaymentId(request.getPaymentId());
                    result.setSuccess(totalAmount.get() > 0);
                    result.setMemo("代付完成");
                    afterHandleChannelNotify(result);

                }
            }, 2, TimeUnit.SECONDS);

            response.setBatchNo(batchNo);
            response.setBatchTransId(batchNo);
            response.setSuccess(true);
            response.setMessage("付款中.");
            return response;
        } catch (Exception e) {
            log.error("发起支付宝代付失败:", e);
            response.setError(ErrorCode.SERVICE_ERROR);
        }
        return response;
    }

    @Override
    public String getChannel() {
        return "alipay-substitute-single-trans";
    }

    @Override
    public String getChannelName() {
        return "支付宝转账到支付宝账户";
    }

    @Override
    public String getChannelProvider() {
        return ChannelProvider.officialAlipay;
    }

    @Override
    public String getChannelProviderName() {
        return "官方支付宝";
    }
}
