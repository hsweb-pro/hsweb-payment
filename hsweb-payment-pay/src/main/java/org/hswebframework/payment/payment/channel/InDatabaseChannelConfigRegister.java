package org.hswebframework.payment.payment.channel;

import org.hswebframework.payment.api.payment.ConfigurablePaymentChannel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;


public class InDatabaseChannelConfigRegister implements BeanPostProcessor {

    @Autowired
    private ApplicationContext applicationContext;

    @SuppressWarnings("all")
    private void doRegister(ConfigurablePaymentChannel<?> paymentChannel) {
        InDatabaseChannelConfigurator channelConfigurator = new InDatabaseChannelConfigurator();
        channelConfigurator.setChannel(paymentChannel.getChannel());
        channelConfigurator.setConfigType(paymentChannel.getConfigType());
        channelConfigurator.setTransType(paymentChannel.getTransType());
        channelConfigurator.setChannelProvider(paymentChannel.getChannelProvider());
        channelConfigurator.setContext(applicationContext);
        paymentChannel.setConfigurator(channelConfigurator);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ConfigurablePaymentChannel) {
            doRegister(((ConfigurablePaymentChannel) bean));
        }
        return bean;
    }
}