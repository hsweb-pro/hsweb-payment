package org.hswebframework.payment.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hswebframework.payment.demo.utils.IDGenerator;
import org.hswebframework.payment.demo.utils.SignUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentService {
    private String merchantId;

    private String secretKey;

    private String apiBaseUrl = "http://localhost:8080/open-api/";

    private String notifyBaseUrl = "http://xxxxx.com/";

    private String proxyHost;

    private int proxyPort;

    private HttpClient httpClient;

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(proxyHost)) {
            //设置代理IP、端口、协议（请分别替换）
            HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");

            //把代理设置到请求配置
            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
            //实例化CloseableHttpClient对象
            httpClient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
        } else {
            httpClient = HttpClientBuilder.create().build();
        }
    }


    public static void main(String[] args) {

    }

    @SneakyThrows
    public JSONObject queryOrder(String orderId, String paymentId) {
        String apiUrl = apiBaseUrl + "payment/query";
        Map<String, Object> param = new TreeMap<>();
        param.put("requestId", IDGenerator.generateId());
        param.put("merchantId", merchantId);
        param.put("orderId", orderId);//商户自己的订单号
        param.put("paymentId", paymentId);//支付订单id,和orderId不能同时为空
        //使用商户密钥签名
        String sign = SignUtils.sign(param, secretKey);
        param.put("sign", sign);

        HttpPost post = new HttpPost(apiUrl);
        post.setEntity(createUrlEncodedFormEntity(param));
        HttpResponse response = httpClient.execute(post);
        String responseBody = new String(EntityUtils.toByteArray(response.getEntity()));

        JSONObject object = JSON.parseObject(responseBody);
        System.out.println("验签[" + object.getString("sign") + "]结果:" + SignUtils.verifySign(object, secretKey));
        System.out.println(JSON.toJSONString(object, SerializerFeature.PrettyFormat));
        return object;
    }

    /**
     * 发起支付请求
     */
    @SneakyThrows
    public JSONObject requestPay(
            String channel,
            String orderId,
            String productId,
            String productName,
            long amount,
            String extraParam) {
        String apiUrl = apiBaseUrl + "payment/gateway";

        Map<String, Object> param = new TreeMap<>();//使用treeMap,将参数排序
        param.put("requestId", IDGenerator.generateId());
        param.put("merchantId", merchantId);
        param.put("amount", String.valueOf(amount));//必填:金额,单位:分
        param.put("orderId", orderId);//必填:商户自己的订单号
        param.put("productId", productId);//必填:产品唯一标识
        param.put("productName", productName); //必填:产品名称
        param.put("notifyUrl", notifyBaseUrl + "payment/notify");//异步通知地址,不能有参数. 接口返回:success则通知成功,否则将阶梯性重试.
        param.put("returnUrl", notifyBaseUrl);//用户支付完成后跳转地址,只有部分渠道有效
        param.put("channel", channel);//支付渠道
        param.put("extraParam", extraParam);//渠道需要的特殊参数
        //使用商户密钥签名
        String sign = SignUtils.sign(param, secretKey);
        param.put("sign", sign);

        System.out.println("发起支付请求:\n" + JSON.toJSONString(param, SerializerFeature.PrettyFormat));

        HttpPost post = new HttpPost(apiUrl);
        post.setEntity(createUrlEncodedFormEntity(param));
        HttpResponse response = httpClient.execute(post);
        String responseBody = new String(EntityUtils.toByteArray(response.getEntity()));

        JSONObject object = JSON.parseObject(responseBody);
        System.out.println("验签[" + object.getString("sign") + "]结果:" + SignUtils.verifySign(object, secretKey));
        System.out.println(JSON.toJSONString(object, SerializerFeature.PrettyFormat));
        return object;
    }

    @SneakyThrows
    public JSONObject confirmQuickPay(
            String paymentId,
            String smsCode) {
        String apiUrl = apiBaseUrl + "payment/quick-confirm";

        Map<String, Object> param = new TreeMap<>();//使用treeMap,将参数排序
        param.put("requestId", IDGenerator.generateId());
        param.put("merchantId", merchantId);
        param.put("paymentId", paymentId);
        param.put("smsCode", smsCode);
        //使用商户密钥签名
        String sign = SignUtils.sign(param, secretKey);
        param.put("sign", sign);

        HttpPost post = new HttpPost(apiUrl);
        post.setEntity(createUrlEncodedFormEntity(param));
        HttpResponse response = httpClient.execute(post);
        String responseBody = new String(EntityUtils.toByteArray(response.getEntity()));

        JSONObject object = JSON.parseObject(responseBody);
        System.out.println("验签[" + object.getString("sign") + "]结果:" + SignUtils.verifySign(object, secretKey));
        System.out.println(JSON.toJSONString(object, SerializerFeature.PrettyFormat));
        return object;
    }


    @SneakyThrows
    public JSONObject requestQuickPay(
            String accountName,
            String accountNumber,
            String phoneNumber,
            String idNumber,
            String orderId,
            String productId,
            String productName,
            long amount,
            String extraParam) {
        String apiUrl = apiBaseUrl + "payment/quick-request";

        Map<String, Object> param = new TreeMap<>();//使用treeMap,将参数排序
        param.put("requestId", IDGenerator.generateId());
        param.put("merchantId", merchantId);
        param.put("amount", String.valueOf(amount));//必填:金额,单位:分
        param.put("orderId", orderId);//必填:商户自己的订单号
        param.put("productId", productId);//必填:产品唯一标识
        param.put("productName", productName); //必填:产品名称
        param.put("notifyUrl", notifyBaseUrl + "payment/notify");//异步通知地址,不能有参数. 接口返回:success则通知成功,否则将阶梯性重试.
        param.put("accountName", accountName);
        param.put("accountNumber", accountNumber);
        param.put("phoneNumber", phoneNumber);
        param.put("idNumber", idNumber);

        param.put("extraParam", extraParam);//渠道需要的特殊参数
        //使用商户密钥签名
        String sign = SignUtils.sign(param, secretKey);
        param.put("sign", sign);

        HttpPost post = new HttpPost(apiUrl);
        post.setEntity(createUrlEncodedFormEntity(param));
        HttpResponse response = httpClient.execute(post);
        String responseBody = new String(EntityUtils.toByteArray(response.getEntity()));

        JSONObject object = JSON.parseObject(responseBody);
        System.out.println("验签[" + object.getString("sign") + "]结果:" + SignUtils.verifySign(object, secretKey));
        System.out.println(JSON.toJSONString(object, SerializerFeature.PrettyFormat));
        return object;
    }

    @SneakyThrows
    public JSONObject requestSubstitute(String payeeType,
                                        String details,
                                        String remark) {

        String apiUrl = apiBaseUrl + "payment/transfer";
        Map<String, Object> param = new TreeMap<>();//使用treeMap,将参数排序
        param.put("requestId", IDGenerator.generateId());
        param.put("transNo", IDGenerator.generateId());
        param.put("merchantId", merchantId);
        param.put("payeeType", payeeType);
        param.put("details", details);
        param.put("orderId", IDGenerator.generateId());
        param.put("notifyUrl", notifyBaseUrl + "payment/notify");
        param.put("remark", remark);
        String sign = SignUtils.sign(param, secretKey);
        param.put("sign", sign);
        System.out.println("发起代付请求:\n" + JSON.toJSONString(param, SerializerFeature.PrettyFormat));

        HttpPost post = new HttpPost(apiUrl);
        post.setEntity(createUrlEncodedFormEntity(param));
        HttpResponse response = httpClient.execute(post);
        String responseBody = new String(EntityUtils.toByteArray(response.getEntity()));

        JSONObject object = JSON.parseObject(responseBody);
        System.out.println("验签[" + object.getString("sign") + "]结果:" + SignUtils.verifySign(object, secretKey));
        System.out.println(JSON.toJSONString(object, SerializerFeature.PrettyFormat));
        return object;
    }

    protected static UrlEncodedFormEntity createUrlEncodedFormEntity(Map<String, Object> params) throws UnsupportedEncodingException {
        List<NameValuePair> nameValuePair = params.entrySet()
                .stream().map(stringStringEntry ->
                        new BasicNameValuePair(stringStringEntry.getKey(), String.valueOf(stringStringEntry.getValue())))
                .collect(Collectors.toList());
        return new UrlEncodedFormEntity(nameValuePair, "UTF-8");
    }


}
