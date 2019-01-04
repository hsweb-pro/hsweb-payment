package org.hswebframework.payment.merchant.service.impl;

import org.hswebframework.payment.api.enums.MerchantConfigKey;
import org.hswebframework.payment.api.merchant.config.MerchantConfigHolder;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.config.StringSourceMerchantConfigHolder;
import org.hswebframework.payment.merchant.entity.MerchantConfigEntity;
import org.hswebframework.payment.merchant.service.MerchantConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Component
public class LocalMerchantConfigManager implements MerchantConfigManager {

    @Autowired
    private MerchantConfigService configService;

    @Override
    public MerchantConfigHolder getConfig(String merchantId, String key) {
        MerchantConfigEntity entity = configService.selectByMerchantIdAndKey(merchantId, key);
        if (entity == null) {
            return MerchantConfigHolder.NULL;
        }
        return new StringSourceMerchantConfigHolder(entity.getValue());
    }

    @Override
    public void saveConfig(String merchantId, MerchantConfigKey key, String value) {
        key.tryValidate(value);
        configService.saveOrUpdate(MerchantConfigEntity
                .builder()
                .merchantId(merchantId)
                .key(key.getValue())
                .value(value)
                .merchantWritable(key.isMerchantWritable())
                .build());
    }
}
