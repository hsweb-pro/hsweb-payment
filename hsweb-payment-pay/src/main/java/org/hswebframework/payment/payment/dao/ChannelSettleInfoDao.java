package org.hswebframework.payment.payment.dao;

import org.hswebframework.payment.payment.entity.ChannelSettleInfoEntity;
import org.apache.ibatis.annotations.Param;
import org.hswebframework.web.dao.CrudDao;

public interface ChannelSettleInfoDao extends CrudDao<ChannelSettleInfoEntity,String>{

    void incrementAmount(@Param("accountNo") String accountNo,@Param("amount") long amount);
}
