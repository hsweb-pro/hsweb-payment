package org.hswebframework.payment.payment.service.impl;

import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.payment.dao.ChannelConfigDao;
import org.hswebframework.payment.payment.dao.ChannelSettleInfoDao;
import org.hswebframework.payment.payment.entity.ChannelConfigEntity;
import org.hswebframework.payment.payment.entity.ChannelSettleInfoEntity;
import org.hswebframework.payment.payment.service.LocalChannelConfigService;
import org.hswebframework.web.commons.entity.DataStatusEnum;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.EnableCacheAllEvictGenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
@CacheConfig(cacheNames = "pay-channel-config")
public class LocalChannelConfigServiceImpl extends EnableCacheAllEvictGenericEntityService<ChannelConfigEntity, String>
        implements LocalChannelConfigService {

    @Autowired
    private ChannelConfigDao configDao;

    @Autowired
    private ChannelSettleInfoDao channelSettleInfoDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public ChannelConfigDao getDao() {
        return configDao;
    }

    @Override
    @CacheEvict(
            allEntries = true
    )
    public String insert(ChannelConfigEntity entity) {
        syncAccount(entity);
        String id = super.insert(entity);
        createUpdate().set(entity::getAccountNo)
                .where(entity::getId)
                .exec();
        return id;
    }

    @Override
    @CacheEvict(
            allEntries = true
    )
    public int updateByPk(String id, ChannelConfigEntity entity) {
        if (StringUtils.isEmpty(entity.getAccountNo())) {
            entity.setAccountNo(null);
        }
        int i = super.updateByPk(id, entity);
        syncAccount(entity = selectByPk(id));
        createUpdate().set(entity::getAccountNo)
                .where(entity::getId)
                .exec();
        return i;
    }

    protected void syncAccount(ChannelConfigEntity entity) {

        Runnable createSettleInfo = () -> {
            ChannelSettleInfoEntity settleInfoEntity = new ChannelSettleInfoEntity();
            settleInfoEntity.setId(IDGenerator.SNOW_FLAKE_STRING.generate());
            settleInfoEntity.setAccountNo(entity.getAccountNo());
            settleInfoEntity.setName(entity.getName() + "结算账户");
            settleInfoEntity.setBalance(0L);
            settleInfoEntity.setComment("自动创建");
            settleInfoEntity.setCreateTime(new Date());
            channelSettleInfoDao.insert(settleInfoEntity);
        };
        if (StringUtils.hasLength(entity.getAccountNo())) {
            ChannelSettleInfoEntity infoEntity = DefaultDSLQueryService.createQuery(channelSettleInfoDao)
                    .where(entity::getAccountNo)
                    .forUpdate()
                    .single();

            if (infoEntity == null) {
                createSettleInfo.run();
            }
        } else {
            entity.setAccountNo(IDGenerator.SNOW_FLAKE_STRING.generate());
            createSettleInfo.run();
        }
    }

    @Override
    @Cacheable(key = "'tras-t:'+#transType.name()+':'+#channel+':'+#provider")
    public List<ChannelConfigEntity> queryByTransTypeAndChannel(TransType transType, String channel, String provider) {
        return createQuery()
                .where(ChannelConfigEntity::getTransType, transType)
                .and(ChannelConfigEntity::getChannel, channel)
                .and(ChannelConfigEntity::getChannelProvider, provider)
                .and(ChannelConfigEntity::getStatus, DataStatusEnum.ENABLED.getValue())
                .listNoPaging();
    }

    @Override
    @Cacheable(key = "'tras-t-i:'+#transType.name()+':'+#channel+':'+#channelId")
    public ChannelConfigEntity getByTransTypeAndChannelId(TransType transType, String channel, String channelId) {
        return selectByPk(channelId);
    }
}
