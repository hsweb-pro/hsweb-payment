package org.hswebframework.payment.payment.dao;

import org.hswebframework.payment.payment.entity.ChannelSettleLogEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.dao.CrudDao;

public interface ChannelSettleLogDao extends CrudDao<ChannelSettleLogEntity, String> {

    long sumAmount(QueryParamEntity entity);

}
