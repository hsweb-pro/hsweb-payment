package org.hswebframework.payment.account.web;

import org.hswebframework.payment.account.dao.entity.AccountTransLogEntity;
import org.hswebframework.payment.account.service.LocalAccountTransLogService;
import org.hswebframework.payment.api.account.AccountTransLog;
import org.hswebframework.payment.api.account.reqeust.AccountTransLogRequest;
import org.hswebframework.payment.api.account.response.AccountTransLogResponse;
import org.hswebframework.payment.api.annotation.CurrentMerchant;
import org.hswebframework.payment.api.enums.AccountTransType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.merchant.AgentMerchant;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.payment.api.payment.events.PaymentCompleteEvent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 资金账户
 *
 * @author Lind
 * @since 1.0
 */
@RestController
@RequestMapping("/account-trans-log")
@Api(tags = "资金账户交易记录", value = "资金账户交易记录")
@Authorize(permission = "account-trans-log")
public class AccountTransLogController implements QueryController<AccountTransLogEntity, String, QueryParamEntity> {

    @Autowired
    private LocalAccountTransLogService accountTransLogService;


    @Override
    public LocalAccountTransLogService getService() {
        return accountTransLogService;
    }

    @GetMapping("/me")
    @ApiOperation("查询代理资金记录")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<AccountTransLogEntity>> queryAccountWithdrawLog(@CurrentMerchant AgentMerchant merchant,
                                                                                       QueryParamEntity entity) {
        entity.excludes("accountName","merchantId","accountNo");
        return entity.toNestQuery(query->query.where("merchantId",merchant.getId()))
                .execute(this::list);
    }


    @GetMapping("/merchant/trans-log")
    @ApiOperation("查询商户资金记录")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<AccountTransLogEntity>> queryMerchantTransLog(@CurrentMerchant Merchant merchant,
                                                                                       QueryParamEntity entity) {
        entity.excludes("accountName","merchantId","accountNo");
        return entity.toNestQuery(query->query.where("merchantId",merchant.getId()))
                .execute(this::list);
    }

}
