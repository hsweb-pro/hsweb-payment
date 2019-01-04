package org.hswebframework.payment.merchant.service.impl;

import org.hswebframework.payment.api.account.AccountService;
import org.hswebframework.payment.api.account.reqeust.AccountCreateRequest;
import org.hswebframework.payment.api.enums.AccountType;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.MerchantStatus;
import org.hswebframework.payment.api.merchant.AgentMerchant;
import org.hswebframework.payment.api.merchant.AgentMerchantService;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.event.MerchantCreatedEvent;
import org.hswebframework.payment.api.merchant.request.AgentRegisterRequest;
import org.hswebframework.payment.api.merchant.request.AgentRegisterResponse;
import org.hswebframework.payment.api.merchant.request.AgentUpdateRequest;
import org.hswebframework.payment.api.merchant.request.AgentUpdateResponse;
import org.hswebframework.payment.api.merchant.response.MerchantUpdateResponse;
import org.hswebframework.payment.merchant.dao.AgentMerchantDao;
import org.hswebframework.payment.merchant.entity.AgentMerchantEntity;
import org.hswebframework.web.commons.bean.BeanValidator;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.EnableCacheAllEvictGenericEntityService;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
@CacheConfig(cacheNames = "payment-agent")
public class LocalAgentMerchantService extends EnableCacheAllEvictGenericEntityService<AgentMerchantEntity, String> implements AgentMerchantService {
    @Autowired
    private AgentMerchantDao agentMerchantDao;
    @Autowired
    private UserService      userService;

    @Autowired
    private AccountService accountService;

    @Override
    @Cacheable(key = "'agent-by-id:'+#id")
    public AgentMerchant getAgentById(String id) {
        return convert(selectByPk(id));
    }

    @Override
    @Cacheable(key = "'agent-by-user-id:'+#userId")
    public AgentMerchant getAgentByUserId(String userId) {
        return convert(createQuery().where("userId", userId).single());
    }

    @Override
    @Cacheable(key = "'all-children-id:'+#agentId")
    public List<String> getAllChildrenAgentId(String agentId) {
        if (StringUtils.isEmpty(agentId)) {
            return new ArrayList<>();
        }
        Function<List<String>, List<String>> getIdByParentId = parentId ->
                createQuery()
                        .select("id")
                        .where()
                        .in("parentId", parentId)
                        .listNoPaging()
                        .stream()
                        .map(AgentMerchantEntity::getId)
                        .collect(Collectors.toList());

        List<String> idList = getIdByParentId.apply(Collections.singletonList(agentId));
        List<String> allChildren = new ArrayList<>();
        for (; !CollectionUtils.isEmpty(idList); ) {
            List<String> repeat = idList.stream()
                    .filter(allChildren::contains)
                    .collect(Collectors.toList());
            if (!repeat.isEmpty()) {
                logger.warn("存在循环关联的代理:{}", repeat);
                idList.removeAll(repeat);
            }
            allChildren.addAll(idList);
            idList = getIdByParentId.apply(idList);
        }
        return allChildren;
    }

    @Override
    @CacheEvict(allEntries = true)
    public AgentUpdateResponse updateAgent(AgentUpdateRequest request) {
        BeanValidator.tryValidate(request);

        AgentUpdateResponse response = new AgentUpdateResponse();

        AgentMerchantEntity merchant = selectByPk(request.getAgentId());
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
    @CacheEvict(allEntries = true)
    public AgentRegisterResponse registerAgent(AgentRegisterRequest request) {
        AgentRegisterResponse registerResponse = new AgentRegisterResponse();
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
        AgentMerchantEntity agentEntity = new AgentMerchantEntity();
        agentEntity.copyFrom(request);
        agentEntity.setUserId(userId);
        agentEntity.setStatus(MerchantStatus.PENDING_REVIEW);
        agentEntity.setCreateTime(new Date());
        //创建资金账户
        String accountNo = IDGenerator.SNOW_FLAKE_STRING.generate();
        agentEntity.setAccountNo(accountNo);

        //先创建商户生成商户id
        String merchantId = insert(agentEntity);
        AccountCreateRequest accountCreateRequest = new AccountCreateRequest();
        accountCreateRequest.setRequestId(IDGenerator.SNOW_FLAKE_STRING.generate());
        accountCreateRequest.setAccountNo(accountNo);
        accountCreateRequest.setAccountType(AccountType.NORMAL);
        accountCreateRequest.setMerchantId(merchantId);
        accountCreateRequest.setName(agentEntity.getName());
        accountCreateRequest.setCreateUser("system");
        //创建资金账户,失败抛出异常,回滚
        accountService.createAccount(accountCreateRequest).assertSuccess();

        registerResponse.setMerchant(convert(agentEntity));
        registerResponse.setSuccess(true);

        return registerResponse;
    }


    public AgentMerchant convert(AgentMerchantEntity entity) {
        if (entity == null) {
            return null;
        }
        return entity.copyTo(new AgentMerchant());
    }

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public AgentMerchantDao getDao() {
        return agentMerchantDao;
    }
}
