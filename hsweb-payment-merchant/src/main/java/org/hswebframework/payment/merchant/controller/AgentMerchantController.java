package org.hswebframework.payment.merchant.controller;

import org.hswebframework.payment.api.enums.MerchantStatus;
import org.hswebframework.payment.api.exception.BusinessException;
import org.hswebframework.payment.api.merchant.AgentMerchant;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.request.AgentRegisterRequest;
import org.hswebframework.payment.api.merchant.request.AgentRegisterResponse;
import org.hswebframework.payment.api.merchant.request.AgentUpdateRequest;
import org.hswebframework.payment.api.merchant.request.AgentUpdateResponse;
import org.hswebframework.payment.api.merchant.response.MerchantRegisterResponse;
import org.hswebframework.payment.api.merchant.response.MerchantUpdateResponse;
import org.hswebframework.payment.merchant.entity.AgentMerchantEntity;
import org.hswebframework.payment.merchant.service.impl.LocalAgentMerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@Authorize(permission = "agent-manager", description = "代理管理")
@RequestMapping("manager/agent")
@Validated
@Api(tags = "代理管理")
public class AgentMerchantController implements QueryController<AgentMerchantEntity, String, QueryParamEntity> {

    @Autowired
    private LocalAgentMerchantService localAgentMerchantService;

    @Override
    public LocalAgentMerchantService getService() {
        return localAgentMerchantService;
    }

    @PostMapping
    @Authorize(action = Permission.ACTION_ADD)
    @ApiOperation("添加代理")
    public ResponseMessage<AgentMerchant> createMerchant(@RequestBody @Valid AgentRegisterRequest request) {
        AgentRegisterResponse response = localAgentMerchantService.registerAgent(request);
        //如果失败抛出异常
        response.assertSuccess();
        return ResponseMessage.ok(response.getMerchant());
    }

    @PutMapping("/{merchantId}/status/{status}")
    @Authorize(action = Permission.ACTION_UPDATE)
    @ApiOperation("修改代理状态")
    public ResponseMessage<Void> createMerchant(@PathVariable String merchantId,
                                                @PathVariable MerchantStatus status) {
        localAgentMerchantService.createUpdate()
                .set("status", status)
                .where("id", merchantId)
                .exec();
        return ResponseMessage.ok();
    }

    @PutMapping("/{agentId}")
    @Authorize(action = Permission.ACTION_UPDATE)
    @ApiOperation("修改代理信息")
    public ResponseMessage<AgentMerchant> createMerchant(@PathVariable String agentId, @RequestBody AgentUpdateRequest request) {
        request.setAgentId(agentId);

        AgentUpdateResponse response = localAgentMerchantService.updateAgent(request);
        //如果失败抛出异常
        response.assertSuccess();
        return ResponseMessage.ok(response.getMerchant());
    }
}
