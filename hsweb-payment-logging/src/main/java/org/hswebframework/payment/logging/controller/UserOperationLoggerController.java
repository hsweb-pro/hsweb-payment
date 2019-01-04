package org.hswebframework.payment.logging.controller;

import org.hswebframework.payment.logging.entity.UserOperationLoggerEntity;
import org.hswebframework.payment.logging.service.UserOperationLoggerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/logger/user")
@Authorize(permission = "logger", description = "日志管理")
@Api("用户操作日志")
public class UserOperationLoggerController implements QueryController<UserOperationLoggerEntity, String, QueryParamEntity> {

    @Autowired
    private UserOperationLoggerService systemLoggerService;

    @Override
    public UserOperationLoggerService getService() {
        return systemLoggerService;
    }

    @GetMapping("/me")
    @Authorize(merge = false)
    @ApiOperation("获取当前登陆用户的操作日志")
    public ResponseMessage<PagerResult<UserOperationLoggerEntity>> me(Authentication me, QueryParamEntity entity) {
        return entity.toNestQuery(query -> query.and("userId", me.getUser().getId())).execute(this::list);
    }
}
