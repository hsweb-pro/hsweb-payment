package org.hswebframework.payment.payment.notify.supports;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hswebframework.payment.api.crypto.CipherManager;
import org.hswebframework.payment.api.crypto.Signature;
import org.hswebframework.payment.api.enums.NotifyType;
import org.hswebframework.payment.payment.notify.Notification;
import org.hswebframework.payment.payment.notify.Notifier;
import org.hswebframework.payment.payment.notify.NotifierProvider;
import org.hswebframework.payment.payment.notify.NotifyResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.hswebframework.expands.request.RequestBuilder;
import org.hswebframework.expands.request.SimpleRequestBuilder;
import org.hswebframework.expands.request.http.HttpRequest;
import org.hswebframework.expands.request.http.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j(topic = "system.payment.notify.http")
@Component
public class HttpNotifierProvider implements NotifierProvider {

    private RequestBuilder requestBuilder = new SimpleRequestBuilder();

    @Autowired
    private CipherManager cipherManager;

    @Override
    public NotifyType getType() {
        return NotifyType.HTTP;
    }

    private RequestConfig commonsConfig = RequestConfig.custom()
            .setConnectTimeout(2000)
            .setSocketTimeout(2000)
            .setConnectionRequestTimeout(5000)
            .build();

    @Override
    public Notifier createNotifier(Notification notification) {
        JSONObject config = new JSONObject(notification.getNotifyConfig());
        String notifyUrl = config.getString("notifyUrl");
        if (notifyUrl == null) {
            throw new IllegalArgumentException("notifyUrl不能为空");
        }
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("paymentId", notification.getPaymentId());
        parameters.put("merchantId", notification.getMerchantId());
        parameters.put("orderId", notification.getOrderId());
        parameters.put("productId", notification.getProductId());
        parameters.put("amount", String.valueOf(notification.getAmount()));
        parameters.put("status", String.valueOf(notification.getStatus().getValue()));
        parameters.put("statusText", String.valueOf(notification.getStatus().getText()));
        parameters.put("completeTime", String.valueOf(notification.getCompleteTime().getTime()));
        parameters.put("signType", Signature.Type.MD5.name());

        if (notification.getExtraParam() != null) {
            parameters.putAll(notification.getExtraParam());
        }
        //获取商户的加密工具
        Signature signature = cipherManager.getSignature(Signature.Type.MD5, notification.getMerchantId());
        //签名
        String sign = signature.sign(parameters);
        //构造请求
        HttpRequest request = requestBuilder.http(notifyUrl)
                .contentType("application/x-www-form-urlencoded")
                .before(nativeRequest -> ((HttpRequestBase) nativeRequest).setConfig(commonsConfig))
                .param("sign", sign)
                .params(parameters);

        return new Notifier() {
            @Override
            public NotifyResult doNotify() {
                try {
                    log.info("执行交易通知:{}\n{}", notifyUrl, JSON.toJSONString(parameters, SerializerFeature.PrettyFormat));

                    Response response = request.post();
                    int code = response.getCode();
                    String result = response.asString();
                    log.info("通知返回状态码:{},内容:{}", code, result);
                    if (code == 200 && "success".equals(result)) {
                        return NotifyResult.success();
                    } else {
                        return NotifyResult.error("通知地址未返回正确结果,status:" + code + ";response:" + result);
                    }
                } catch (Exception e) {
                    log.warn("通知失败:{}", e.getMessage(), e);
                    return NotifyResult.error(e.getMessage());
                }
            }

            @Override
            public void cleanup() {
//                try {
//                    request.close();
//                } catch (IOException e) {
//                    log.error("close http request error", e);
//                }
            }
        };
    }

}
