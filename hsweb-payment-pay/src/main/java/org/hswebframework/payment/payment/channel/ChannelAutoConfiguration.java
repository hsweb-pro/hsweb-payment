package org.hswebframework.payment.payment.channel;

import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.ConfigurablePaymentChannel;
import org.hswebframework.payment.api.payment.PaymentChannelConfigurator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <pre class="code yml">
 * hsweb:
 * pay:
 * channels:
 * - trans_type: GATEWAY
 * channel: alipay
 * configs:
 * - appId: xxx
 * - appKey: xxx
 * </pre>
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class ChannelAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "hsweb.pay")
    public ChannelProperties channelProperties() {
        return new ChannelProperties();
    }

    @Bean
    @ConditionalOnProperty(name = "hsweb.pay.channel.config-type", havingValue = "db")
    public InDatabaseChannelConfigRegister inDatabaseChannelConfigRegister() {
        return new InDatabaseChannelConfigRegister();
    }

    @Bean
    @ConditionalOnProperty(name = "hsweb.pay.channel.config-type", havingValue = "prop", matchIfMissing = true)
    public ChannelConfigAutoRegister channelConfigAutoRegister() {
        return new ChannelConfigAutoRegister();
    }

    class ChannelConfigAutoRegister implements BeanPostProcessor {

        @Autowired
        private ChannelProperties channelProperties;

        @PostConstruct
        public void init() {
            channelProperties.validate();
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof ConfigurablePaymentChannel) {
                ConfigurablePaymentChannel channel = ((ConfigurablePaymentChannel) bean);
                channel.setConfigurator(channelProperties.createConfig(channel.getTransType(), channel.getChannel(), channel.getConfigType()));
            }
            return bean;
        }
    }

    @Getter
    @Setter
    public static class ChannelProperties {
        private List<ChannelConfig> channels = new ArrayList<>();

        public void validate() {
            for (int i = 0; i < channels.size(); i++) {
                channels.get(0).validate(i);
            }
        }

        public <T extends org.hswebframework.payment.api.payment.ChannelConfig> PaymentChannelConfigurator<T> createConfig(TransType transType, String channel, Class<T> type) {
            List<Map<String, Object>> allConfig = channels.stream()
                    .filter(config -> transType == config.transType && channel.equalsIgnoreCase(config.channel))
                    .findFirst()
                    .map(ChannelConfig::getConfigs)
                    .orElse(Collections.emptyList());

            return new RoundPaymentChannelConfigurator<T>(type) {
                @Override
                protected List<?> getAllConfig() {
                    return allConfig;
                }

            };
        }
    }

    @Getter
    @Setter
    public static class ChannelConfig {
        private TransType transType;
        private String channel;
        private List<Map<String, Object>> configs;

        public void validate(int index) {
            Assert.notNull(transType, "请添加配置:hsweb.pay.channels[" + index + "].trans-type");
            Assert.hasText(channel, "请添加配置:hsweb.pay.channels[" + index + "].channel");
            Assert.notNull(configs, "请添加配置:hsweb.pay.channels[" + index + "].configs");

            for (int i = 0; i < configs.size(); i++) {
                Assert.notNull(configs.get(i).get("id"), "请添加配置:hsweb.pay.channels[" + index + "].configs[" + i + "].id");
            }


        }
    }
}
