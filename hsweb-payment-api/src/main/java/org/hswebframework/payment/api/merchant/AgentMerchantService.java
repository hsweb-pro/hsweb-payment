package org.hswebframework.payment.api.merchant;

import org.hswebframework.payment.api.merchant.request.*;
import org.hswebframework.payment.api.merchant.request.AgentUpdateResponse;
import org.hswebframework.payment.api.merchant.request.AgentRegisterRequest;
import org.hswebframework.payment.api.merchant.request.AgentRegisterResponse;
import org.hswebframework.payment.api.merchant.request.AgentUpdateRequest;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface AgentMerchantService {

    AgentMerchant getAgentById(String id);

    AgentMerchant getAgentByUserId(String userId);

    /**
     * 获取所有的下级代理ID
     */
    List<String> getAllChildrenAgentId(String agentId);

    /**
     * 注册代理商户
     *
     * @param request 注册请求
     * @return 注册结果
     */
    AgentRegisterResponse registerAgent(AgentRegisterRequest request);

    AgentUpdateResponse updateAgent(AgentUpdateRequest request);

}
