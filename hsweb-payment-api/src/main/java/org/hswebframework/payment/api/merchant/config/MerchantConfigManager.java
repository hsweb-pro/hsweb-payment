package org.hswebframework.payment.api.merchant.config;

import org.hswebframework.payment.api.enums.MerchantConfigKey;

import java.util.List;
import java.util.Optional;

/**
 * 商户配置管理器,用于管理商户的一些配置,如商户密钥,收费规则,结算配置等等
 *
 * @author zhouhao
 * @see MerchantConfigKey
 * @since 1.0.0
 */
public interface MerchantConfigManager {

    MerchantConfigHolder getConfig(String merchantId, String key);

    void saveConfig(String merchantId, MerchantConfigKey key, String value);

    @SuppressWarnings("all")
    default <T> Optional<T> getConfig(String merchantId, MerchantConfigKey key) {
        return getConfig(merchantId, key.getValue()).as((Class<T>) key.getType());
    }

    @SuppressWarnings("all")
    default <T> Optional<List<T>> getConfigList(String merchantId, MerchantConfigKey key) {
        return getConfig(merchantId, key.getValue()).asList((Class<T>) key.getType());
    }
}
