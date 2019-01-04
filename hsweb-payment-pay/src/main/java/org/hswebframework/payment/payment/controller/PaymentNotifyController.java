package org.hswebframework.payment.payment.controller;

import org.hswebframework.payment.payment.entity.NotificationLogEntity;
import org.hswebframework.payment.payment.notify.NotifyResult;
import org.hswebframework.payment.payment.service.impl.LocalNotifyLogService;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@Authorize(permission = "payment")
@RequestMapping("payment/notify/log")
public class PaymentNotifyController implements QueryController<NotificationLogEntity, String, QueryParamEntity> {

    @Autowired
    private LocalNotifyLogService localNotifyLogService;

    @Override
    public LocalNotifyLogService getService() {
        return localNotifyLogService;
    }

    @PostMapping("/{id}/retry")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<NotifyResult> doNotify(@PathVariable String id) {
        return ResponseMessage.ok(localNotifyLogService.retryNotify(id));
    }
}
