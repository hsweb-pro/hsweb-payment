package org.hswebframework.payment.payment.service;

import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.payment.entity.ChannelConfigEntity;
import org.hswebframework.web.service.CrudService;

import java.util.List;

public interface LocalChannelConfigService extends CrudService<ChannelConfigEntity,String>{

    List<ChannelConfigEntity> queryByTransTypeAndChannel(TransType transType,String channel,String provider);

    ChannelConfigEntity getByTransTypeAndChannelId(TransType transType,String channel,String channelId);
}
