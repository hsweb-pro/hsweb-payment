package org.hswebframework.payment.api.utils;

import org.hswebframework.payment.api.enums.MerchantConfigKey;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.config.MerchantRateConfig;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class MerchantRateChargeUtils {

    public static Optional<MerchantRateConfig> findConfig(MerchantConfigManager configManager, String merchantId, TransType transType, String channel) {


        return configManager.<MerchantRateConfig>getConfigList(merchantId, MerchantConfigKey.RATE_CONFIG)
                .map(list -> {
                    //默认配置
                    AtomicReference<MerchantRateConfig> defaultConfig = new AtomicReference<>(null);
                    return list.stream()
                            //加载默认配置
                            .peek(rateConfig -> {
                                if (transType == rateConfig.getTransType() && StringUtils.isEmpty(rateConfig.getChannel())) {
                                    defaultConfig.set(rateConfig);
                                }
                            })
                            //找完全匹配
                            .filter(rateConfig -> transType == rateConfig.getTransType() && (channel == null || channel.equals(rateConfig.getChannel())))
                            .findFirst()
                            .orElseGet(defaultConfig::get);
                });

    }
}
