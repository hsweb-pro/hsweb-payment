package org.hswebframework.payment.account.web;
import org.hswebframework.payment.account.dao.entity.AccountEntity;
import org.hswebframework.payment.account.service.LocalAccountService;
import org.hswebframework.payment.api.annotation.CurrentMerchant;
import org.hswebframework.payment.api.merchant.Merchant;
import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 资金账户
 * @author Lind
 * @since 1.0
 */
@RestController
@RequestMapping("/account")
@Api(tags = "资金账户",value = "资金账户")
@Authorize(permission = "account")
public class AccountController implements SimpleGenericEntityController<AccountEntity,String, QueryParamEntity> {

    @Autowired
    private LocalAccountService accountService;


    @Override
    public LocalAccountService getService() {
        return accountService;
    }


    @GetMapping("/me-balance")
    @Authorize(merge = false)
    public ResponseMessage<Long> queryBalance(@CurrentMerchant String merchantId){
        return ResponseMessage.ok(accountService.queryBalanceByMerchantId(merchantId));
    }
}
