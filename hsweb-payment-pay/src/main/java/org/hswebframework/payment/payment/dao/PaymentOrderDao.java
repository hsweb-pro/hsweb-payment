package org.hswebframework.payment.payment.dao;

import org.hswebframework.payment.payment.entity.GroupByChannelResult;
import org.hswebframework.payment.payment.entity.PaymentOrderEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.dao.CrudDao;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface PaymentOrderDao extends CrudDao<PaymentOrderEntity, String> {

    long sumAmount(QueryParamEntity entity);

    List<GroupByChannelResult> sumAmountGroupByChannel(QueryParamEntity entity);

    List<GroupByChannelResult> countGroupByChannel(QueryParamEntity entity);

}
