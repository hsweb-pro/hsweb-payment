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
public class MerchantRegisterRequest extends ApiRequest {

    @NotBlank(message = "注册商户名不能为空")
    private String name;

    @NotBlank(message = "注册商户电话不能为空")
    private String phone;

    @NotBlank(message = "注册商户用户名不能为空")
    private String username;

    @NotBlank(message = "注册商户密码不能为空")
    private String password;

    //上级代理Id
    private String agentId;

    private String email;

}
