package org.hswebframework.payment.logging.controller;

import org.hswebframework.payment.logging.entity.AccessLoggerEntity;
import org.hswebframework.payment.logging.service.AccessLoggerService;
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
@RequestMapping("/logger/access")
@Authorize(permission = "logger", description = "日志管理")
@Api("访问日志")
public class AccessLoggerController implements QueryController<AccessLoggerEntity, String, QueryParamEntity> {

    @Autowired
    private AccessLoggerService accessLoggerService;

    @Override
    public AccessLoggerService getService() {
        return accessLoggerService;
    }
}
