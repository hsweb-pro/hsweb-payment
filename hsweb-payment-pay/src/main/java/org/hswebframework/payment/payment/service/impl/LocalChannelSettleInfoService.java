package org.hswebframework.payment.payment.service.impl;

import org.hswebframework.payment.payment.dao.ChannelSettleInfoDao;
import org.hswebframework.payment.payment.entity.ChannelSettleInfoEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
public class LocalChannelSettleInfoService extends GenericEntityService<ChannelSettleInfoEntity, String> {

    @Autowired
    private ChannelSettleInfoDao channelSettleInfoDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public ChannelSettleInfoDao getDao() {
        return channelSettleInfoDao;
    }

    @Override
    public int updateByPk(String id, ChannelSettleInfoEntity entity) {
        entity.setAccountNo(null);//不能修改账户名
        entity.setBalance(null);
        return super.updateByPk(id, entity);
    }

    @Override
    public String insert(ChannelSettleInfoEntity entity) {
        entity.setBalance(0L);
        entity.setCreateTime(new Date());
        return super.insert(entity);
    }
}
