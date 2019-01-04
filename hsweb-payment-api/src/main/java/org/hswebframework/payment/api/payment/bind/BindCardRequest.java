package org.hswebframework.payment.api.payment.bind;

import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.enums.BankCode;
import org.hswebframework.payment.api.enums.BindCardPurpose;
import org.hswebframework.payment.api.enums.IdType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class BindCardRequest extends ApiRequest {

    //绑卡用途
    @NotNull(message = "绑卡用途不能为空")
    private BindCardPurpose purpose;

    //绑卡渠道
    @NotBlank(message = "绑卡渠道不能为空")
    private String channel;

    @NotBlank(message = "商户ID不能为空")
    private String merchantId;

    @NotNull(message = "银行代码不能为空")
    private BankCode bankCode;

    @NotBlank(message = "户名不能为空")
    private String accountName;

    @NotBlank(message = "帐号不能为空")
    private String accountNo;

    @NotNull(message = "证件类型不能为空")
    private IdType idType;

    @NotBlank(message = "证件号码不能为空")
    private String idNumber;

    @NotBlank(message = "手机号码不能为空")
    private String phoneNumber;

    @NotBlank(message = "绑卡类型不能为空")
    private String cardType;

    private String validDate;

    private String cvn2;

}
