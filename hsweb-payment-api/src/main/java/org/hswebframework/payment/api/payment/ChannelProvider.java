package org.hswebframework.payment.api.payment;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ChannelProvider {

    /**
     * 官方支付宝
     */
    String officialAlipay = "official-alipay";

    /**
     * 官方微信
     */
    String officialWechat = "official-wechat";


    /**
     * @return 渠道供应商, 如: native-alipay
     */
    String getChannelProvider();

    /**
     * @return 渠道供应商名称, 如: 官方支付宝,四方支付宝
     */
    String getChannelProviderName();

    Class<? extends ChannelConfig> getConfigType();

}
