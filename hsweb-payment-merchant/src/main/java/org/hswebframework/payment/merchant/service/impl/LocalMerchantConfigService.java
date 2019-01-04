package org.hswebframework.payment.merchant.service.impl;

import org.hswebframework.payment.merchant.dao.MerchantConfigDao;
import org.hswebframework.payment.merchant.entity.MerchantConfigEntity;
import org.hswebframework.payment.merchant.events.MerchantConfigModifiedEvent;
import org.hswebframework.payment.merchant.service.MerchantConfigService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@CacheConfig(cacheNames = "hsweb:merchant:config")
@Component
public class LocalMerchantConfigService extends GenericEntityService<MerchantConfigEntity, String> implements MerchantConfigService {

    @Autowired
    private MerchantConfigDao merchantConfigDao;

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public MerchantConfigDao getDao() {
        return merchantConfigDao;
    }

    @Override
    @CacheEvict(key = "'m:'+#entity.merchantId+'k:'+#entity.key")
    public String insert(MerchantConfigEntity entity) {
        String id = super.insert(entity);
        eventPublisher.publishEvent(new MerchantConfigModifiedEvent(entity));
        return id;
    }

    @Override
    @CacheEvict(key = "'m:'+#entity.merchantId+'k:'+#entity.key")
    public int updateByPk(String s, MerchantConfigEntity entity) {
        int i = super.updateByPk(s, entity);
        if (i > 0) {
            eventPublisher.publishEvent(new MerchantConfigModifiedEvent(entity));
        }
        return i;
    }

    @Override
    @CacheEvict(key = "'m:'+#entity.merchantId+'k:'+#entity.key")
    public String saveOrUpdate(MerchantConfigEntity entity) {
        return super.saveOrUpdate(entity);
    }

    @Override
    @CacheEvict(key = "'m:'+#result.merchantId+'k:'+#result.key")
    public MerchantConfigEntity deleteByPk(String s) {
        MerchantConfigEntity entity = super.deleteByPk(s);
        if (null != entity) {
            eventPublisher.publishEvent(new MerchantConfigModifiedEvent(entity));
        }
        return entity;
    }

    @Override
    @Cacheable(key = "'m:'+#merchantId+'k:'+#key")
    public MerchantConfigEntity selectByMerchantIdAndKey(String merchantId, String key) {
        if (StringUtils.isEmpty(merchantId) || StringUtils.isEmpty(key)) {
            return null;
        }
        return createQuery()
                .where("merchantId", merchantId)
                .and("key", key)
                .single();
    }

    @Override
    protected boolean dataExisted(MerchantConfigEntity entity) {
        MerchantConfigEntity configEntity = selectByMerchantIdAndKey(entity.getMerchantId(), entity.getKey());
        if (configEntity != null) {
            entity.setId(configEntity.getId());
            return true;
        }
        return false;
    }
}
