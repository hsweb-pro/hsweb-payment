package org.hswebframework.payment.api.payment;


import org.hswebframework.payment.api.enums.TransType;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface PaymentChannelConfigurator<T extends ChannelConfig> {
    /**
     * 获取一个渠道的配置,如果配置不存在返回<code>null</code>
     *
     * @param transType 交易类型
     * @return 渠道配置内容
     */
    T getPaymentConfigByMerchantId(String merchantId, TransType transType,long amount);

    /**
     * 根据渠道ID返回渠道配置
     *
     * @param channelId 渠道ID
     * @return 渠道配置, 不存在返回null
     */
    T getPaymentConfigById(String channelId);
}
