package org.hswebframework.payment.payment.controller;

import org.hswebframework.payment.payment.entity.MerchantBindPaymentChannelEntity;
import org.hswebframework.payment.payment.service.impl.LocalMerchantBindPaymentChannelService;
import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/channel/bind")
@Authorize(permission = "channel-config", description = "渠道配置")
@Api(tags = "渠道配置", description = "渠道配置")
public class MerchantChannelBindController implements SimpleGenericEntityController<MerchantBindPaymentChannelEntity, String, QueryParamEntity> {

    @Autowired
    private LocalMerchantBindPaymentChannelService bindPaymentChannelService;

    @Override
    public LocalMerchantBindPaymentChannelService getService() {
        return bindPaymentChannelService;
    }

}
