package org.hswebframework.payment.logging.controller;

import org.hswebframework.payment.logging.entity.SystemLoggerEntity;
import org.hswebframework.payment.logging.service.SystemLoggerService;
import io.swagger.annotations.Api;
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
@RequestMapping("/logger/system")
@Authorize(permission = "logger", description = "日志管理")
@Api("系统日志")
public class SystemLoggerController implements QueryController<SystemLoggerEntity, String, QueryParamEntity> {

    @Autowired
    private SystemLoggerService systemLoggerService;

    @Override
    public SystemLoggerService getService() {
        return systemLoggerService;
    }
}
