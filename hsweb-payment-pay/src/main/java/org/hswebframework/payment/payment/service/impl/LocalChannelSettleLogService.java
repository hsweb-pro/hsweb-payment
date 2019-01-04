package org.hswebframework.payment.payment.service.impl;

import org.hswebframework.payment.payment.dao.ChannelSettleLogDao;
import org.hswebframework.payment.payment.entity.ChannelSettleLogEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
public class LocalChannelSettleLogService extends GenericEntityService<ChannelSettleLogEntity, String> {

    @Autowired
    private ChannelSettleLogDao channelSettleLogDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public ChannelSettleLogDao getDao() {
        return channelSettleLogDao;
    }
}
