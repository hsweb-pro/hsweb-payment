package org.hswebframework.payment.payment.channel.weixin;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayMwebOrderResult;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.ActiveQuerySupportPaymentChannel;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.PaymentResponse;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.payment.channel.AbstractPaymentChannel;
import org.hswebframework.payment.payment.entity.PaymentOrderEntity;
import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.bean.BeanValidator;
import org.hswebframework.web.validate.ValidationException;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URLEncoder;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j(topic = "system.payment.wx-h5.official")
@Controller
public class WxH5PaymentChannel extends AbstractPaymentChannel<WxPayChannelConfig, PaymentRequest, WxH5PaymentChannel.WeixinH5PaymentResponse>
        implements ActiveQuerySupportPaymentChannel {

    //重定向到微信h5支付页面
    @GetMapping("/payment/weixin/redirect")
    @SneakyThrows
    public void redirectWeixinH5Page(@RequestParam String url,
                                     @RequestParam String orderId,
                                     HttpServletResponse response) {
        MDC.put("businessId", orderId);
        PaymentOrderEntity entity = orderService.selectByPk(orderId);
        if (entity != null && entity.getStatus() != PaymentStatus.paying) {
            PaymentRequest request = getOriginalRequestResponse(orderId).getRequest();
            url = request.getReturnUrl();
            log.info("订单[{}]已支付,跳转到商户页面.", orderId);
        }

        PrintWriter writer = response.getWriter();
        if (StringUtils.isEmpty(url)) {

            return;
        }
        writer.write("<script type='text/javascript'>");
        writer.write("window.location=\"" + url + "\"");
        writer.write("</script>");

    }

    //定时查询未支付的订单时自动调用
    @Override
    @SneakyThrows
    public void doActiveQueryOrderResult(PaymentOrder order) {
        WxPayService payService = getConfigurator().getPaymentConfigById(order.getChannelId())
                .createWxPayService();
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

    //接收微信通知
    @PostMapping(value = "/notify/wx-h5/{channelId}", produces = MediaType.APPLICATION_XML_VALUE)
    @SneakyThrows
    @ResponseBody
    public String handelNotify(@PathVariable String channelId, @RequestBody String xmlData) {
        try {
            WxPayService payService = getConfigurator().getPaymentConfigById(channelId).createWxPayService();
            final WxPayOrderNotifyResult notifyResult = payService.parseOrderNotifyResult(xmlData);
            String orderId = notifyResult.getOutTradeNo();
            afterHandleChannelNotify(ChannelNotificationResult
                    .builder()
                    .success("SUCCESS".equalsIgnoreCase(notifyResult.getReturnCode())
                            && "SUCCESS".equalsIgnoreCase(notifyResult.getResultCode()))
                    .amount(notifyResult.getTotalFee() == null ? 0 : notifyResult.getTotalFee())
                    .paymentId(orderId)
                    .resultObject(notifyResult)
                    .build());
            return WxPayNotifyResponse.success("成功");
        } catch (Exception e) {
            return WxPayNotifyResponse.fail(e.getMessage());
        }
    }

    //发起微信h5支付请求
    @Override
    @SneakyThrows
    protected WeixinH5PaymentResponse doRequestPay(WxPayChannelConfig config, PaymentRequest request) {
        if (config == null) {
            throw ErrorCode.CHANNEL_CONFIG_ERROR.createException();
        }
        try {
            WxPayUnifiedOrderRequestValidate order = new WxPayUnifiedOrderRequestValidate();
            if (request.getExtraParam() != null) {
                FastBeanCopier.copy(request.getExtraParam(), order);
            }
            order.setBody(getOrderComment(config,request));
            order.setOutTradeNo(request.getPaymentId());
            order.setTotalFee(Long.valueOf(request.getAmount()).intValue());
            order.setNotifyUrl(getNotifyLocation(config) + "notify/wx-h5/" + config.getId());
            order.setFeeType(request.getCurrency());
            order.setMchId(config.getMchId());
            order.setTradeType(WxPayConstants.TradeType.MWEB);
            BeanValidator.tryValidate(order);
            log.info("发起微信h5支付,请求报文:{}", order.toXML());
            WxPayMwebOrderResult result = config.createWxPayService().createOrder(order);
            WeixinH5PaymentResponse response = FastBeanCopier.copy(request, WeixinH5PaymentResponse::new);
            response.setPayUrl(getServerLocation(config) +
                    "payment/weixin/redirect?url=" +
                    URLEncoder.encode(result.getMwebUrl(), "UTF-8") + "&orderId=" + request.getPaymentId());
            response.setSuccess(true);
            return response;
        } catch (WxPayException e) {
            log.error("发起微信h5支付失败:{}:{}", e.getErrCode(), e.getMessage(), e);
            throw ErrorCode.CHANNEL_RETURN_ERROR.createException();
        } catch (ValidationException e) {
            throw ErrorCode.ILLEGAL_PARAMETERS.createException(e.getMessage());
        }
    }

    @Getter
    @Setter
    public static class WeixinH5PaymentResponse extends PaymentResponse {
        private String payUrl;
    }

    @Override
    public TransType getTransType() {
        return TransType.GATEWAY;
    }

    @Override
    public String getChannel() {
        return "weixin-h5";
    }

    @Override
    public String getChannelName() {
        return "H5-微信支付";
    }

    @Override
    public String getChannelProvider() {
        return officialWechat;
    }

    @Override
    public String getChannelProviderName() {
        return "官方微信支付";
    }

}
