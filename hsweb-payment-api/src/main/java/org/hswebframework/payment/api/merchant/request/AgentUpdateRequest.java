package org.hswebframework.payment.api.merchant.request;

import org.hswebframework.payment.api.ApiRequest;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class AgentUpdateRequest extends ApiRequest {

    @NotBlank(message = "id不能为空")
    private String agentId;

    private String name;

    private String phone;

    private String username;

    private String password;

    //上级代理Id
    private String parentId;

    private String email;
}
