package org.hswebframework.payment.merchant.service.impl;

import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.api.account.AccountService;
import org.hswebframework.payment.api.account.reqeust.AccountCreateRequest;
import org.hswebframework.payment.api.enums.AccountType;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.MerchantConfigKey;
import org.hswebframework.payment.api.enums.MerchantStatus;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.config.MerchantServiceConfig;
import org.hswebframework.payment.api.merchant.event.MerchantCreatedEvent;
import org.hswebframework.payment.api.merchant.request.MerchantRegisterRequest;
import org.hswebframework.payment.api.merchant.request.MerchantUpdateRequest;
import org.hswebframework.payment.api.merchant.response.MerchantRegisterResponse;
import org.hswebframework.payment.api.merchant.response.MerchantUpdateResponse;
import org.hswebframework.payment.merchant.dao.MerchantDao;
import org.hswebframework.payment.merchant.entity.MerchantEntity;
import org.hswebframework.payment.merchant.service.MerchantProperties;
import org.hswebframework.web.commons.bean.BeanValidator;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
public class LocalMerchantService extends GenericEntityService<MerchantEntity, String> implements MerchantService {

    @Autowired
    private MerchantDao merchantDao;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MerchantConfigManager configManager;

    @Autowired
    private MerchantProperties merchantProperties;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public MerchantDao getDao() {
        return merchantDao;
    }

    public Merchant convert(MerchantEntity entity) {
        if (entity == null) {
            return null;
        }
        return entity.copyTo(new Merchant());
    }

    @Override
    public Merchant getMerchantById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return convert(selectByPk(id));
    }

    @Override
    public Merchant getMerchantByUserId(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        return convert(createQuery().where(Merchant::getUserId, userId).single());
    }

    @Override
    public MerchantUpdateResponse updateMerchant(MerchantUpdateRequest request) {
        BeanValidator.tryValidate(request);

        MerchantUpdateResponse response = new MerchantUpdateResponse();

        MerchantEntity merchant = selectByPk(request.getMerchantId());
        if (merchant == null) {
            throw ErrorCode.MERCHANT_NOT_EXISTS.createException();
        }
        merchant.copyFrom(request);
        //修改密码
        if (StringUtils.hasText(request.getPassword())) {
            UserEntity userEntity = userService.createEntity();
            userEntity.setName(merchant.getName());
            userEntity.setPassword(request.getPassword());
            userService.update(merchant.getUserId(), userEntity);
        }
        //修改商户
        updateByPk(merchant.getId(), merchant);
        response.setSuccess(true);
        response.setMerchant(convert(merchant));
        return response;
    }

    @Override
    public MerchantRegisterResponse registerMerchant(MerchantRegisterRequest request) {
        MerchantRegisterResponse registerResponse = new MerchantRegisterResponse();
        BeanValidator.tryValidate(request);
        if (userService.selectByUsername(request.getUsername()) != null) {
            throw ErrorCode.USERNAME_ALREADY_EXISTS.createException();
        }
        //创建用户
        UserEntity userEntity = userService.createEntity();
        userEntity.setName(request.getName());
        userEntity.setUsername(request.getUsername());
        userEntity.setPassword(request.getPassword());
        String userId = userService.insert(userEntity);
        //创建商户
        MerchantEntity merchantEntity = new MerchantEntity();
        merchantEntity.copyFrom(request);
        merchantEntity.setUserId(userId);
        merchantEntity.setStatus(MerchantStatus.PENDING_REVIEW);
        merchantEntity.setCreateTime(new Date());
        merchantEntity.setAgentId(request.getAgentId());
        //创建资金账户
        String accountNo = IDGenerator.SNOW_FLAKE_STRING.generate();
        merchantEntity.setAccountNo(accountNo);

        //先创建商户生成商户id
        String merchantId = insert(merchantEntity);
        AccountCreateRequest accountCreateRequest = new AccountCreateRequest();
        accountCreateRequest.setRequestId(IDGenerator.SNOW_FLAKE_STRING.generate());
        accountCreateRequest.setAccountNo(accountNo);
        accountCreateRequest.setAccountType(AccountType.NORMAL);
        accountCreateRequest.setMerchantId(merchantId);
        accountCreateRequest.setName(merchantEntity.getName());
        accountCreateRequest.setCreateUser("system");
        //创建资金账户,失败抛出异常,回滚
        accountService.createAccount(accountCreateRequest).assertSuccess();
        registerResponse.setMerchant(convert(merchantEntity));
        registerResponse.setSuccess(true);

        eventPublisher.publishEvent(new MerchantCreatedEvent(registerResponse.getMerchant()));
        return registerResponse;
    }

    @EventListener
    public void handleMerchantCreatedEvent(MerchantCreatedEvent event) {
        Merchant merchant = event.getMerchant();
        //创建密钥
        configManager.saveConfig(merchant.getId(), MerchantConfigKey.SECRET_KEY, IDGenerator.MD5.generate().toUpperCase());

        //复杂代理配置
        if (StringUtils.hasText(merchant.getAgentId())) {
            //开通服务
            configManager.getConfig(merchant.getAgentId(), MerchantConfigKey.SUPPORTED_SERVICE.getValue())
                    .asString()
                    .ifPresent(conf -> configManager.saveConfig(merchant.getId(), MerchantConfigKey.SUPPORTED_SERVICE, conf));

            //开通渠道
            configManager.getConfig(merchant.getAgentId(), MerchantConfigKey.SUPPORTED_CHANNEL.getValue())
                    .asString()
                    .ifPresent(conf -> configManager.saveConfig(merchant.getId(), MerchantConfigKey.SUPPORTED_CHANNEL, conf));

            //费率渠道
            configManager.getConfig(merchant.getAgentId(), MerchantConfigKey.RATE_CONFIG.getValue())
                    .asString()
                    .ifPresent(conf -> configManager.saveConfig(merchant.getId(), MerchantConfigKey.RATE_CONFIG, conf));

        } else {
            //开通默认服务
            List<MerchantServiceConfig> serviceConfigs = merchantProperties.getDefaultServiceConfig();
            if (!CollectionUtils.isEmpty(serviceConfigs)) {
                configManager.saveConfig(merchant.getId(), MerchantConfigKey.SUPPORTED_SERVICE, JSON.toJSONString(serviceConfigs));
            }
        }

    }

    /**
     * 根据用户ID查询商户信息
     *
     * @param userId
     * @return
     */
    public MerchantEntity queryMerchantByUserId(String userId) {
        return createQuery().where("userId", userId).single();
    }

    /**
     * 更新备案信息
     *
     * @param merchant
     * @return
     */
    public boolean updateMerchantMainInfo(String id, Merchant merchant) {
        createUpdate()
                .set("productName", merchant.getProductName())
                .set("legalPersonName", merchant.getLegalPersonName())
                .set("legalPersonIdCard", merchant.getLegalPersonIdCard())
                .set("companyName", merchant.getCompanyName())
                .set("companyAddress", merchant.getCompanyAddress())
                .set("idCardFront", merchant.getIdCardFront())
                .set("idCardBack", merchant.getIdCardBack())
                .set("businessLicense", merchant.getBusinessLicense())
                .where("id", id)
                .exec();
        return true;
    }


    /**
     * 查询代理商下属商户
     *
     * @param agentId
     * @return
     */
    public List<MerchantEntity> queryMerchantByAgentId(String agentId) {
        return createQuery().where("agentId", agentId).listNoPaging();
    }

    /**
     * 修改联系信息
     *
     * @param merchant
     * @return
     */
    public boolean updateMerchantBaseInfo(String id, Merchant merchant) {
        createUpdate()
                .where("id", id)
                .set("email", merchant.getEmail())
                .set("phone", merchant.getPhone())
                .set("qq", merchant.getQq())
                .set("weChat", merchant.getWeChat())
                .exec();
        return true;
    }
}
