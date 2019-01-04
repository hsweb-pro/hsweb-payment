package org.hswebframework.payment.payment.channel.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.ActiveQuerySupportPaymentChannel;
import org.hswebframework.payment.api.payment.ChannelProvider;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.PaymentResponse;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.api.utils.Money;
import org.hswebframework.payment.payment.channel.AbstractPaymentChannel;
import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.bean.FastBeanCopier;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j(topic = "system.payment.alipay.official")
public abstract class AbstractOfficialAlipayChannel extends AbstractPaymentChannel<AlipayConfig, PaymentRequest, PaymentResponse>
        implements ActiveQuerySupportPaymentChannel {


    private AlipayClient createAlipayClient(AlipayConfig config) {
        return new DefaultAlipayClient(
                config.getUrl(), config.getAppId(), config.getRsaPrivateKey(),
                "json", "utf-8", config.getPublicKey(), config.getSignType(),
                config.getProxyHost(), config.getProxyPort());
    }

    @Override
    protected PaymentResponse doRequestPay(AlipayConfig config, PaymentRequest request) {
        if (config == null) {
            throw ErrorCode.CHANNEL_CONFIG_ERROR.createException();
        }
        try {
            AlipayClient client = createAlipayClient(config);
            RequestType productCode = getType();
            AlipayRequest aliRequest = productCode.createRequest(request,
                    getNotifyLocation(config)
                            + "notify/alipay/" + config.getId());

            PaymentResponse response = FastBeanCopier.copy(request, productCode.execute(client, aliRequest));
            
            response.setSuccess(true);

            return response;
        } catch (AlipayApiException e) {
            log.warn("发起支付宝支付失败", e);
            throw ErrorCode.CHANNEL_RETURN_ERROR.createException(e);
        }
    }

    @Override
    public TransType getTransType() {
        return TransType.GATEWAY;
    }


    @Override
    public String getChannelProvider() {
        return ChannelProvider.officialAlipay;
    }

    @Override
    public String getChannelProviderName() {
        return "官方支付宝";
    }

    protected abstract RequestType getType();

    @Override
    @SneakyThrows
    public void doActiveQueryOrderResult(PaymentOrder order) {
        AlipayConfig config = getConfigurator().getPaymentConfigById(order.getChannelId());

        AlipayClient client = createAlipayClient(config);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(order.getId());
        request.setBizModel(model);

        AlipayTradeQueryResponse response = client.execute(request);
        String transStatus = response.getTradeStatus();
        String amount = response.getTotalAmount();
        if (transStatus == null) {
            return;
        }
        //交易成功才发送事件
        if (transStatus.equals("TRADE_SUCCESS") || transStatus.equals("TRADE_FINISHED")) {
            afterHandleChannelNotify(new ChannelNotificationResult(
                    order.getId(),
                    true,
                    Money.amout(amount).getCent(),
                    "",
                    response
            ));
        }

    }
}
