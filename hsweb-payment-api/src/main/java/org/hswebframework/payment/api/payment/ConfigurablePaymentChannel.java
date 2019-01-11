package org.hswebframework.payment.api.payment;


import org.hswebframework.payment.api.enums.TransType;

/**
 * 可配置的支付渠道
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface ConfigurablePaymentChannel<T extends ChannelConfig> extends ChannelProvider {

    TransType getTransType();

    String getChannel();

    PaymentChannelConfigurator<T> getConfigurator();

    void setConfigurator(PaymentChannelConfigurator<T> configurator);

    /**
     * @return 配置类型
     */
    Class<T> getConfigType();
}
