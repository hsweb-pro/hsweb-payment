package org.hswebframework.payment.payment.channel;

import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.ChannelConfig;
import org.hswebframework.payment.api.payment.PaymentChannelConfigurator;
import lombok.Getter;
import lombok.SneakyThrows;
import org.hswebframework.web.bean.FastBeanCopier;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public abstract class RoundPaymentChannelConfigurator<T extends ChannelConfig> implements PaymentChannelConfigurator<T> {

    public RoundPaymentChannelConfigurator(Class<T> type) {
        this.type = type;
    }

    @Getter
    private Class<T> type;

    protected abstract List<?> getAllConfig();

    private volatile AtomicInteger lastIndex = new AtomicInteger(0);


    @Override
    public T getPaymentConfigById(String channelId) {
        if (getAllConfig().size() == 1) {
            return convertConfig(getAllConfig().get(0));
        }
        return getAllConfig().stream()
                .map(this::convertConfig)
                .filter(conf -> channelId.equals(conf.getId()))
                .findFirst().orElse(null);
    }

    @SneakyThrows
    protected T convertConfig(Object config) {
        if (null == config) {
            return null;
        }
        if (type.isInstance(config)) {
            return ((T) config);
        } else {
            return FastBeanCopier.copy(config, type.newInstance());
        }
    }

    @Override
    @SneakyThrows
    public T getPaymentConfigByMerchantId(String merchantId, TransType transType,long amount) {
        List<?> allConfig = getAllConfig();
        Object config;
        if (allConfig == null || allConfig.isEmpty()) {
            return null;
        }
        if (allConfig.size() == 1) {
            config = allConfig.get(0);
        } else {
            config = allConfig.get(Math.min(allConfig.size() - 1, lastIndex.getAndIncrement()));
            if (lastIndex.get() >= allConfig.size()) {
                lastIndex.set(0);
            }
        }
        return convertConfig(config);
    }


}
