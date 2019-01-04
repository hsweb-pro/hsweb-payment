package org.hswebframework.payment.merchant.controller;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.MerchantConfigKey;
import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.enums.WithdrawType;
import org.hswebframework.payment.api.merchant.MerchantPayeeService;
import org.hswebframework.payment.api.merchant.MerchantWithdrawLog;
import org.hswebframework.payment.api.merchant.WithdrawService;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.config.MerchantSettleConfig;
import org.hswebframework.payment.api.merchant.payee.MerchantPayee;
import org.hswebframework.payment.api.annotation.CurrentMerchant;
import org.hswebframework.payment.api.merchant.request.ApplyWithdrawRequest;
import org.hswebframework.payment.api.merchant.request.CloseWithdrawRequest;
import org.hswebframework.payment.api.merchant.request.HandlerWithdrawRequest;
import org.hswebframework.payment.api.merchant.request.QueryWithdrawRequest;
import org.hswebframework.payment.api.merchant.response.ApplyWithdrawResponse;
import org.hswebframework.payment.api.merchant.response.CloseWithdrawResponse;
import org.hswebframework.payment.api.merchant.response.HandlerWithdrawResponse;
import org.hswebframework.payment.api.merchant.response.QueryWithdrawResponse;
import org.hswebframework.payment.merchant.controller.request.WithdrawRequest;
import org.hswebframework.payment.merchant.entity.MerchantWithdrawEntity;
import org.hswebframework.payment.merchant.service.MerchantWithdrawService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.TwoFactor;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.hswebframework.payment.api.enums.WithdrawStatus.APPLYING;

/**
 * @author Lind
 * @since 1.0
 */
@RestController
@RequestMapping("/merchant-withdraw")
@Api(tags = "商户提现", value = "商户提现")
@Authorize(permission = "merchant-withdraw")
public class MerchantWithdrawController implements QueryController<MerchantWithdrawEntity, String, QueryParamEntity> {


    @Autowired
    private MerchantWithdrawService merchantWithdrawService;

    @Autowired
    private WithdrawService withdrawService;


//    @Autowired
//    private MerchantAuthenticatorManager authenticatorManager;

    @Autowired
    private MerchantConfigManager configManager;

    @Override
    public MerchantWithdrawService getService() {
        return merchantWithdrawService;
    }

    @PostMapping("/apply")
    @ApiOperation("提现申请")
    @Authorize(merge = false)
    @TwoFactor(value = "withdraw", timeout = 0) //2步验证
    public ResponseMessage<ApplyWithdrawResponse> applyWithdraw(@CurrentMerchant(agentOrMerchant = true) String currentMerchantId,
                                                                @RequestBody WithdrawRequest withdrawRequest) {
        MerchantSettleConfig config = configManager.<MerchantSettleConfig>getConfig(currentMerchantId, MerchantConfigKey.SETTLE_CONFIG)
                .orElseThrow(() -> ErrorCode.MERCHANT_CONFIG_ERROR.createException("商户结算信息未配置"));

        ApplyWithdrawRequest request = new ApplyWithdrawRequest();
        request.setAmount(withdrawRequest.getTransAmount());
        request.setApplyTime(new Date());
        //统一为手动提现
        request.setWithdrawType(WithdrawType.MANUAL);
        request.setMerchantId(currentMerchantId);
        request.setPayee(config);
        //目前仅支持提现到银行卡
        request.setPayeeType(PayeeType.BANK);

        ApplyWithdrawResponse response = withdrawService.applyWithdraw(request);
        response.assertSuccess();
        return ResponseMessage.ok(response);
    }

    @PostMapping("/apply/{merchantId}")
    @ApiOperation("发起提现申请")
    @Authorize(action = Permission.ACTION_ADD)//后台管理权限
    public ResponseMessage<ApplyWithdrawResponse> adminApplyWithdraw(@PathVariable String merchantId,
                                                                     @RequestBody WithdrawRequest withdrawRequest) {
        return applyWithdraw(merchantId, withdrawRequest);
    }

    @PostMapping("/handle/{withdrawId}/{channelId}")
    @ApiOperation("确认提现申请")
    @Authorize(action = "handle", description = "确认提现申请")
    public ResponseMessage<HandlerWithdrawResponse> handlerWithdraw(@PathVariable String withdrawId,
                                                                    @PathVariable String channelId) {
        HandlerWithdrawRequest request = new HandlerWithdrawRequest();
        request.setWithdrawId(withdrawId);
        request.setChannelId(channelId);
        HandlerWithdrawResponse response = withdrawService.handleWithdraw(request);
        response.assertSuccess();
        return ResponseMessage.ok(response);
    }

    @PostMapping("/close/{withdrawId}")
    @ApiModelProperty("关闭提现申请")
    @Authorize(action = "close", description = "关闭提现")
    public ResponseMessage<CloseWithdrawResponse> closeWithdraw(@PathVariable String withdrawId,
                                                                @RequestBody String comment) {
        MerchantWithdrawEntity entity = merchantWithdrawService.selectByPk(withdrawId);
        if (entity == null) {
            throw new NotFoundException("提现记录不存在");
        }
        if (entity.getStatus() != APPLYING) {
            throw new NotFoundException("无法关闭[" + entity.getStatus().getText() + "]的提现");
        }
        CloseWithdrawRequest request = new CloseWithdrawRequest();
        request.setWithdrawId(withdrawId);
        request.setComment(comment);
        CloseWithdrawResponse response = withdrawService.closeWithdraw(request);
        response.assertSuccess();
        return ResponseMessage.ok(response);
    }

    @GetMapping("/log")
    @ApiModelProperty("提现申请记录")
    @Authorize(merge = false)
    public ResponseMessage<QueryWithdrawResponse> withdrawLog(@CurrentMerchant String currentMerchantId, QueryParamEntity entity) {
        QueryWithdrawRequest request = new QueryWithdrawRequest();
        request.setMerchantId(currentMerchantId);


        //追加 merchant_id=? 查询条件
        entity.toNestQuery(query -> query.and(MerchantWithdrawEntity::getMerchantId, currentMerchantId));

        PagerResult<MerchantWithdrawEntity> result = this.list(entity).getResult();

        List<MerchantWithdrawLog> collect = result.getData()
                .stream()
                .map(e -> FastBeanCopier.copy(e, new MerchantWithdrawLog()))
                .collect(Collectors.toList());

        QueryWithdrawResponse response = new QueryWithdrawResponse();
        response.setSuccess(true);
        response.setWithdrawLogList(collect);
        response.setTotal(result.getTotal());
        return ResponseMessage.ok(response);
    }

    @PostMapping("/close-applying/{withdrawId}")
    @ApiModelProperty("客户关闭提现申请")
    @Authorize(merge = false)
    public ResponseMessage<CloseWithdrawResponse> merchantCloseWithdraw(@PathVariable String withdrawId,
                                                                        @CurrentMerchant String currentMerchantId) {
        MerchantWithdrawEntity entity = merchantWithdrawService.queryWithdrawLogByIdAndMerchantId(currentMerchantId, withdrawId);
        if (entity == null) {
            throw new NotFoundException("提现记录不存在");
        }
        CloseWithdrawRequest request = new CloseWithdrawRequest();
        if (entity.getStatus() == APPLYING) {
            request.setWithdrawId(withdrawId);
            CloseWithdrawResponse response = withdrawService.closeWithdraw(request);
            response.assertSuccess();
            return ResponseMessage.ok(response);
        } else {
            return ResponseMessage.ok();
        }
    }
}


