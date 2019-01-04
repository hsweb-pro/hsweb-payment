package org.hswebframework.payment.payment.channel.alipay;

import com.alipay.api.internal.util.AlipaySignature;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.ConfigurablePaymentChannel;
import org.hswebframework.payment.api.payment.PaymentChannelConfigurator;
import org.hswebframework.payment.api.utils.Money;
import org.hswebframework.payment.payment.events.ChannelNotificationEvent;
import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
@Controller
public class AliPayNotifyController implements ConfigurablePaymentChannel<AlipayConfig> {

    private PaymentChannelConfigurator<AlipayConfig> configurator;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping(value = "notify/alipay/{channelId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String channelNotify(@PathVariable String channelId, @RequestParam Map<String, String> params) {
        AlipayConfig config = getConfigurator().getPaymentConfigById(channelId);
        if (config == null) {
            log.error("收到支付宝异步通知,但是未获取到渠道配置[{}],通知内容:{}", channelId, params);
            return "fail";
        }
        try {
            boolean verifySuccess = AlipaySignature.rsaCheckV1(params, config.getPublicKey(), "utf-8", config.getSignType());
            if (!verifySuccess) {
                log.warn("验证签名失败:channelId:[{}],data:{}", channelId, params);
                return "fail";
            }
            String transStatus = params.get("trade_status");
            String tradeNo = params.get("trade_no");
            String paymentId = params.get("out_trade_no");
            String amount = params.get("total_amount");
            MDC.put("businessId", paymentId);
            //transStatus=TRADE_FINISHED
            //如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
            //如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。

            //transStatus=TRADE_SUCCESS
            //如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            eventPublisher.publishEvent(new ChannelNotificationEvent(new ChannelNotificationResult(
                    paymentId,
                    transStatus.equals("TRADE_SUCCESS") || transStatus.equals("TRADE_FINISHED"),
                    Money.amout(amount).getCent(),
                    "",
                    params
            )));
            return "success";
        } catch (Exception e) {
            log.warn("处理支付宝通知失败", e);
        }
        return "fail";
    }

    @Override
    public TransType getTransType() {
        return TransType.GATEWAY;
    }

    @Override
    public String getChannel() {
        return "alipay";
    }

    @Override
    public PaymentChannelConfigurator<AlipayConfig> getConfigurator() {
        return configurator;
    }

    @Override
    public void setConfigurator(PaymentChannelConfigurator<AlipayConfig> configurator) {
        this.configurator = configurator;
    }

    @Override
    public String getChannelProvider() {
        return officialAlipay;
    }

    @Override
    public String getChannelProviderName() {
        return "官方支付宝";
    }

    @Override
    public Class<AlipayConfig> getConfigType() {
        return AlipayConfig.class;
    }
}
