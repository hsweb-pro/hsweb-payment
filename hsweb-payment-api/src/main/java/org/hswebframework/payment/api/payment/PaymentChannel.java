package org.hswebframework.payment.api.payment;


import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.ApiResponse;
import org.hswebframework.payment.api.enums.TransType;

/**
 * 支付渠道接口
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface PaymentChannel<REQ extends ApiRequest, RES extends ApiResponse> extends ChannelProvider {

    /**
     * @return 交易类型
     */
    TransType getTransType();

    /**
     * @return 渠道标识, 如微信扫码支付, 支付宝支付
     */
    String getChannel();

    /**
     * @return 渠道名称
     */
    String getChannelName();

    /**
     * 发起支付请求
     *
     * @param request 支付请求对象
     * @return 支付请求结果
     */
    RES requestPayment(REQ request);

    Class<REQ> getRequestType();

    Class<RES> getResponseType();

    default boolean match(REQ request) {
        return true;
    }
}
