package org.hswebframework.payment.payment.service.impl;

import org.hswebframework.payment.payment.dao.MerchantBindPaymentChannelDao;
import org.hswebframework.payment.payment.entity.MerchantBindPaymentChannelEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.EnableCacheAllEvictGenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
@CacheConfig(cacheNames = "pay-mer-conf-bind")
public class LocalMerchantBindPaymentChannelService extends EnableCacheAllEvictGenericEntityService<MerchantBindPaymentChannelEntity, String> {

    @Autowired
    private MerchantBindPaymentChannelDao merchantBindPaymentChannelDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public MerchantBindPaymentChannelDao getDao() {
        return merchantBindPaymentChannelDao;
    }

    /**
     * 查询本商户没有绑定,但是其他商户绑定了的配置
     *
     * @param merchantId 商户id
     * @param configId   配置id集合
     * @return 其他商户绑定的配置
     */
    @Cacheable(key = "'not-in-merchant-'+#merchantId+'-'+#configId.hashCode()")
    public List<String> selectNotInMerchantBind(String merchantId, List<String> configId) {
        if (CollectionUtils.isEmpty(configId)) {
            return new ArrayList<>();
        }
        return createQuery().select("configId")
                .where()
                .not("merchantId", merchantId)
                .in("configId", configId)
                .listNoPaging()
                .stream()
                .map(MerchantBindPaymentChannelEntity::getConfigId)
                .collect(Collectors.toList());
    }
}
