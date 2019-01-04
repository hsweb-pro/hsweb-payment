package org.hswebframework.payment.payment.controller;

import org.hswebframework.payment.payment.entity.ChannelSettleInfoEntity;
import org.hswebframework.payment.payment.service.impl.LocalChannelSettleInfoService;
import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.CreateController;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.UpdateController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/channel/settle/info")
@Authorize(permission = "channel-settle", description = "渠道结算信息")
@Api(tags = "渠道结算信息", description = "渠道结算信息")
public class ChannelSettleInfoController implements
        QueryController<ChannelSettleInfoEntity, String, QueryParamEntity>,
        UpdateController<ChannelSettleInfoEntity, String, ChannelSettleInfoEntity>,
        CreateController<ChannelSettleInfoEntity, String, ChannelSettleInfoEntity> {

    @Autowired
    private LocalChannelSettleInfoService settleInfoService;

    @Override
    public LocalChannelSettleInfoService getService() {
        return settleInfoService;
    }

    @Override
    public ChannelSettleInfoEntity modelToEntity(ChannelSettleInfoEntity model, ChannelSettleInfoEntity entity) {
        return model;
    }
}
