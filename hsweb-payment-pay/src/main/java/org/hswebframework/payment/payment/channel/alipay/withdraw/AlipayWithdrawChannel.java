package org.hswebframework.payment.payment.channel.alipay.withdraw;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayFundTransToaccountTransferModel;
import com.alipay.api.request.AlipayFundTransOrderQueryRequest;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.response.AlipayFundTransOrderQueryResponse;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.ActiveQuerySupportPaymentChannel;
import org.hswebframework.payment.api.payment.ChannelConfig;
import org.hswebframework.payment.api.payment.ChannelProvider;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.payment.withdraw.WithdrawPaymentChannel;
import org.hswebframework.payment.api.payment.withdraw.WithdrawPaymentRequest;
import org.hswebframework.payment.api.utils.Money;
import org.hswebframework.payment.payment.channel.AbstractPaymentChannel;
import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 支付宝提现渠道
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
@Component
public class AlipayWithdrawChannel extends AbstractPaymentChannel<AlipayWithdrawConfig,
        WithdrawPaymentRequest<Payee>, AlipayWithdrawResponse>
        implements WithdrawPaymentChannel<AlipayWithdrawResponse, Payee>
        , ActiveQuerySupportPaymentChannel {
    @Override
    public PayeeType getPayeeType() {
        return PayeeType.ALIPAY;
    }

    private AlipayClient createAlipayClient(AlipayWithdrawConfig config) {
        return new DefaultAlipayClient(
                config.getUrl(), config.getAppId(), config.getRsaPrivateKey(),
                "json", "utf-8", config.getPublicKey(), config.getSignType(),
                config.getProxyHost(), config.getProxyPort());
    }

    @Override
    public TransType getTransType() {
        return TransType.WITHDRAW;
    }

    @Override
    protected AlipayWithdrawResponse doRequestPay(AlipayWithdrawConfig config, WithdrawPaymentRequest<Payee> request) {
        AlipayClient client = createAlipayClient(config);
        AlipayWithdrawResponse response = new AlipayWithdrawResponse();

        try {

            AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();

            model.setPayeeAccount(request.getPayee().getPayee());
            model.setPayeeRealName(request.getPayee().getPayeeName());
            model.setPayeeType("ALIPAY_LOGONID");
            model.setAmount(Money.cent(request.getAmount()).toString());
            model.setPayerShowName(config.getPayerShowName());
            model.setPayerRealName(config.getPayerRealName());
            model.setRemark(request.getRemark());
            model.setOutBizNo(request.getPaymentId());

            AlipayFundTransToaccountTransferRequest transferRequest = new AlipayFundTransToaccountTransferRequest();
//            transferRequest.setNotifyUrl(getNotifyLocation() + "alipay/withdraw/notify/" + request.getPaymentId());
            transferRequest.setBizModel(model);
            AlipayFundTransToaccountTransferResponse channelResponse = client.execute(transferRequest);
            response.setChannelOrderId(channelResponse.getOrderId());
            response.setSuccess(channelResponse.isSuccess());
            //同步返回
            if (channelResponse.isSuccess()) {
                runLater(() -> {
                    afterHandleChannelNotify(ChannelNotificationResult.builder()
                            .paymentId(request.getPaymentId())
                            .amount(request.getAmount())
                            .success(true)
                            .build());
                }, 1, TimeUnit.SECONDS);
            }
            return response;
        } catch (AlipayApiException e) {
            log.error("调用支付宝付款接口失败:", e);
            response.setError(ErrorCode.CHANNEL_RETURN_ERROR.createException(e.getErrMsg()));
        } catch (Exception e) {
            log.error("调用支付宝付款接口失败:", e);
            response.setError(ErrorCode.SERVICE_ERROR);
        }
        return null;
    }

    @Override
    public String getChannel() {
        return "alipy-withdraw";
    }

    @Override
    public String getChannelName() {
        return "支付宝付款提现";
    }

    @Override
    @SneakyThrows
    public void doActiveQueryOrderResult(PaymentOrder order) {
        AlipayWithdrawConfig config = getConfigurator().getPaymentConfigById(order.getChannelId());
        AlipayClient client = createAlipayClient(config);
        AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_biz_no", order.getId());

        request.setBizContent(jsonObject.toJSONString());
        AlipayFundTransOrderQueryResponse response = client.execute(request);
        log.debug("查询到支付宝转账结果:{}", JSON.toJSONString(response, SerializerFeature.PrettyFormat));
        if (response.isSuccess()) {
            boolean success = "SUCCESS".equalsIgnoreCase(response.getStatus());
            if (success) {
                afterHandleChannelNotify(ChannelNotificationResult.builder()
                        .paymentId(order.getId())
                        .amount(order.getAmount())
                        .success(true)
                        .build());
            }
            boolean failed = "FAIL".equalsIgnoreCase(response.getStatus())
                    || "REFUND".equalsIgnoreCase(response.getStatus());
            if (failed) {
                afterHandleChannelNotify(ChannelNotificationResult.builder()
                        .paymentId(order.getId())
                        .memo(response.getFailReason())
                        .amount(order.getAmount())
                        .success(false)
                        .build());
            }
        }
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
