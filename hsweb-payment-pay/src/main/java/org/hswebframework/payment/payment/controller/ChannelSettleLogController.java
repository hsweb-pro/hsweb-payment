package org.hswebframework.payment.payment.controller;

import org.hswebframework.payment.payment.entity.ChannelSettleLogEntity;
import org.hswebframework.payment.payment.service.impl.LocalChannelSettleLogService;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/channel/settle/log")
@Authorize(permission = "channel-settle", description = "渠道结算信息")
public class ChannelSettleLogController implements QueryController<ChannelSettleLogEntity, String, QueryParamEntity> {

    @Autowired
    private LocalChannelSettleLogService settleLogService;

    @Override
    public LocalChannelSettleLogService getService() {
        return settleLogService;
    }

}
