package org.hswebframework.payment.merchant.controller;

import org.hswebframework.payment.api.annotation.CurrentMerchant;
import org.hswebframework.payment.api.enums.MerchantConfigKey;
import org.hswebframework.payment.api.enums.MerchantStatus;
import org.hswebframework.payment.api.exception.BusinessException;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.request.MerchantRegisterRequest;
import org.hswebframework.payment.api.merchant.request.MerchantUpdateRequest;
import org.hswebframework.payment.api.merchant.response.MerchantRegisterResponse;
import org.hswebframework.payment.api.merchant.response.MerchantUpdateResponse;
import org.hswebframework.payment.merchant.entity.MerchantEntity;
import org.hswebframework.payment.merchant.service.impl.LocalMerchantService;
import org.hswebframework.payment.merchant.service.impl.TotpTwoFactorEmailNotifier;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@Authorize(permission = "merchant-manager", description = "商户管理")
@RequestMapping("manager/merchant")
@Validated
@Api(tags = "商户管理")
public class MerchantController implements QueryController<MerchantEntity, String, QueryParamEntity> {
    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MerchantConfigManager configManager;

    @Autowired
    private LocalMerchantService localMerchantService;


    @Autowired
    private TotpTwoFactorEmailNotifier emailNotifier;

    @PostMapping("/email/totp/{userId}")
    @Authorize(action = Permission.ACTION_GET)
    @ApiOperation("发送totp邮件")
    public ResponseMessage<Void> sendEmail(@PathVariable String userId) {
        emailNotifier.sendTotpEmail(userId);
        return ResponseMessage.ok();
    }

    @PostMapping
    @Authorize(action = Permission.ACTION_ADD)
    @ApiOperation("添加商户")
    public ResponseMessage<Merchant> createMerchant(@RequestBody @Valid MerchantRegisterRequest request) {
        MerchantRegisterResponse response = merchantService.registerMerchant(request);
        //如果失败抛出异常
        response.assertSuccess(BusinessException::new);
        return ResponseMessage.ok(response.getMerchant());
    }


    @PutMapping("/{merchantId}/status/{status}")
    @Authorize(action = Permission.ACTION_UPDATE)
    @ApiOperation("修改商户状态")
    public ResponseMessage<Void> createMerchant(@PathVariable String merchantId,
                                                @PathVariable MerchantStatus status) {
        localMerchantService.createUpdate()
                .set("status", status)
                .where("id", merchantId)
                .exec();

        return ResponseMessage.ok();
    }

    @PutMapping("/{merchantId}")
    @Authorize(action = Permission.ACTION_UPDATE)
    @ApiOperation("修改商户信息")
    public ResponseMessage<Merchant> createMerchant(@PathVariable String merchantId, @RequestBody MerchantUpdateRequest request) {
        request.setMerchantId(merchantId);
        MerchantUpdateResponse response = merchantService.updateMerchant(request);
        return ResponseMessage.ok(response.getMerchant());
    }

    @PatchMapping("/me/config/{key}")
    @Authorize(merge = false)//不合并类上的注解,这里只需要登录就可以修改配置
    @ApiOperation("商户修改自己的配置")
    public ResponseMessage<Void> updateMeConfig(@CurrentMerchant String currentMerchantId,//当前登录用户
                                                @PathVariable MerchantConfigKey key,
                                                @RequestBody String configValue) {
        Assert.isTrue(key.isMerchantWritable(), "无法修改此配置");
        configManager.saveConfig(currentMerchantId, key, configValue);
        return ResponseMessage.ok();

    }

    @GetMapping("/{merchantId}/configs")
    @Authorize(action = Permission.ACTION_UPDATE)
    @ApiOperation("获取商户的全部配置")
    public ResponseMessage<Map<String, String>> updateConfig(@PathVariable String merchantId) {

        Map<String, String> configValue = new HashMap<>();
        for (MerchantConfigKey configKey : MerchantConfigKey.values()) {
            configValue.put(configKey.getValue(), configManager
                    .getConfig(merchantId, configKey.getValue())
                    .getValue()
                    .map(String::valueOf)
                    .orElse(null));
        }
        return ResponseMessage.ok(configValue);

    }

    @PatchMapping("/{merchantId}/config/{key}")
    @Authorize(action = Permission.ACTION_UPDATE)
    @ApiOperation("修改商户配置")
    public ResponseMessage<Void> updateConfig(@PathVariable String merchantId,
                                              @PathVariable MerchantConfigKey key,
                                              @RequestBody String configValue) {
        configManager.saveConfig(merchantId, key, configValue);
        return ResponseMessage.ok();

    }

    @Override
    public LocalMerchantService getService() {
        return localMerchantService;
    }
}
