package org.hswebframework.payment.merchant.dao;

import org.hswebframework.payment.merchant.entity.SubstituteDetailEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.dao.CrudDao;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface SubstituteDetailDao extends CrudDao<SubstituteDetailEntity, String> {
    long sumAmount(QueryParamEntity entity);
}
