package org.hswebframework.payment.account.web;

import io.swagger.annotations.Api;
import org.hswebframework.payment.account.dao.entity.AccountFreezeLogEntity;
import org.hswebframework.payment.account.service.LocalAccountFreezeLogService;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资金账户
 *
 * @author zhouhao
 * @since 1.0
 */
@RestController
@RequestMapping("/account-freeze-log")
@Api(tags = "资金账户冻结记录", value = "资金账户冻结记录")
@Authorize(permission = "account")
public class AccountFreezeLogController implements QueryController<AccountFreezeLogEntity, String, QueryParamEntity> {

    @Autowired
    private LocalAccountFreezeLogService localAccountFreezeLogService;


    @Override
    public LocalAccountFreezeLogService getService() {
        return localAccountFreezeLogService;
    }



}
