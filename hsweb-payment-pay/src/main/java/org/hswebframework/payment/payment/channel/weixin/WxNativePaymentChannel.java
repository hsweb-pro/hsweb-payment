package org.hswebframework.payment.payment.channel.weixin;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayNativeOrderResult;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.ActiveQuerySupportPaymentChannel;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.PaymentResponse;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.payment.channel.AbstractPaymentChannel;
import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.bean.BeanValidator;
import org.hswebframework.web.validate.ValidationException;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j(topic = "system.payment.wx-native.official")
@RestController
public class WxNativePaymentChannel extends
        AbstractPaymentChannel<WxPayChannelConfig, PaymentRequest, WxNativePaymentChannel.WxNativePaymentResponse>
        implements ActiveQuerySupportPaymentChannel {

    @Override
    public TransType getTransType() {
        return TransType.GATEWAY;
    }

    @Override
    public String getChannel() {
        return "weixin-native";
    }

    @Override
    public String getChannelName() {
        return "扫码-微信支付";
    }

    @Override
    @SneakyThrows
    public void doActiveQueryOrderResult(PaymentOrder order) {
        WxPayService payService = getConfigurator().getPaymentConfigById(order.getChannelId()).createWxPayService();

        WxPayOrderQueryResult result = payService.queryOrder(null, order.getId());
        log.info("主动查询{}订单:[{}],返回:{}", getChannelName(), order.getId(), result.toString());

        //用户未支付
        if (!("NOTPAY".equalsIgnoreCase(result.getTradeState()) || "USERPAYING".equalsIgnoreCase(result.getTradeState()))) {
            ChannelNotificationResult notificationResult = ChannelNotificationResult
                    .builder()
                    .success("SUCCESS".equalsIgnoreCase(result.getTradeState())
                            && "SUCCESS".equalsIgnoreCase(result.getReturnCode())
                            && "SUCCESS".equalsIgnoreCase(result.getResultCode()))
                    .amount(result.getTotalFee() == null ? 0 : result.getTotalFee())
                    .paymentId(order.getId())
                    .memo(result.getTradeStateDesc())
                    .resultObject(result)
                    .build();
            afterHandleChannelNotify(notificationResult);
        }
    }

    @PostMapping(value = "/notify/wx-native/{channelId}", produces = MediaType.APPLICATION_XML_VALUE)
    @SneakyThrows
    public String handelNotify(@PathVariable String channelId, @RequestBody String xmlData) {
        try {
            WxPayService payService = getConfigurator().getPaymentConfigById(channelId).createWxPayService();
            final WxPayOrderNotifyResult notifyResult = payService.parseOrderNotifyResult(xmlData);
            String orderId = notifyResult.getOutTradeNo();
            MDC.put("businessId", orderId);

            afterHandleChannelNotify(ChannelNotificationResult
                    .builder()
                    .success("SUCCESS".equalsIgnoreCase(notifyResult.getReturnCode())
                            && "SUCCESS".equalsIgnoreCase(notifyResult.getResultCode()))
                    .amount(notifyResult.getTotalFee() == null ? 0 : notifyResult.getTotalFee())
                    .paymentId(orderId)
                    .memo(notifyResult.getReturnMsg())
                    .resultObject(notifyResult)
                    .build());
            return WxPayNotifyResponse.success("成功");
        } catch (Exception e) {
            log.warn("处理{}通知失败,data:{}", getChannelName(), xmlData, e);
            return WxPayNotifyResponse.fail(e.getMessage());
        }
    }

    @Override
    protected WxNativePaymentResponse doRequestPay(WxPayChannelConfig config, PaymentRequest request) {
        if (config == null) {
            throw ErrorCode.CHANNEL_CONFIG_ERROR.createException();
        }
        try {
            WxPayUnifiedOrderRequestValidate order = new WxPayUnifiedOrderRequestValidate();
            if (request.getExtraParam() != null) {
                FastBeanCopier.copy(request.getExtraParam(), order);
            }
            order.setBody(request.getProductName());
            order.setOutTradeNo(request.getPaymentId());
            order.setTotalFee(Long.valueOf(request.getAmount()).intValue());
            order.setNotifyUrl(getNotifyLocation(config) + "notify/wx-native/" + config.getId());
            order.setFeeType(request.getCurrency());
            order.setProductId(request.getProductId());
            order.setTradeType(WxPayConstants.TradeType.NATIVE);
            BeanValidator.tryValidate(order);
            log.info("发起微信扫码支付,请求报文:{}", order.toXML());
            WxPayNativeOrderResult result = config.createWxPayService().createOrder(order);
            WxNativePaymentResponse response = FastBeanCopier.copy(request, WxNativePaymentResponse::new);
            response.setSuccess(true);
            response.setQrCode(result.getCodeUrl());

            return response;
        } catch (WxPayException e) {
            log.error("发起微信支付失败:{}:{}", e.getErrCode(), e.getMessage(), e);
            throw ErrorCode.CHANNEL_RETURN_ERROR.createException();
        } catch (ValidationException e) {
            throw ErrorCode.ILLEGAL_PARAMETERS.createException(e.getMessage());
        }
    }

    @Override
    public String getChannelProvider() {
        return officialWechat;
    }

    @Override
    public String getChannelProviderName() {
        return "官方微信支付";
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    public static class WxNativePaymentResponse extends PaymentResponse {
        private String qrCode;
    }


}
