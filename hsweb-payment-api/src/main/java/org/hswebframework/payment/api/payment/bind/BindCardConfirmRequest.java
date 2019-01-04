package org.hswebframework.payment.api.payment.bind;

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
public class BindCardConfirmRequest extends ApiRequest {

    @NotBlank(message = "绑卡流水号不能为空")
    private String bindId;

    @NotBlank(message = "短信验证码不能为空")
    private String smsCode;
}
