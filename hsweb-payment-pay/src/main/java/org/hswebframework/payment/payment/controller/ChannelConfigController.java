package org.hswebframework.payment.payment.controller;

import org.hswebframework.payment.payment.entity.ChannelConfigEntity;
import org.hswebframework.payment.payment.service.LocalChannelConfigService;
import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/channel/config")
@Authorize(permission = "channel-config", description = "渠道配置")
@Api(tags = "渠道配置", description = "渠道配置")
public class ChannelConfigController implements SimpleGenericEntityController<ChannelConfigEntity, String, QueryParamEntity> {

    @Autowired
    private LocalChannelConfigService channelConfigService;

    @Override
    public CrudService<ChannelConfigEntity, String> getService() {
        return channelConfigService;
    }

}
