package org.hswebframework.payment.api.account.reqeust;

import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.enums.AccountType;
import org.hswebframework.payment.api.enums.CurrencyEnum;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateRequest extends ApiRequest {

    @NotBlank(message = "资金账户号不能为空")
    private String accountNo;

    @NotBlank(message = "商户ID不能为空")
    private String merchantId;

    @NotBlank(message = "账户名称不能为空")
    private String name;

    @NotBlank(message = "账户类型不能为空")
    private AccountType accountType;

    @NotBlank(message = "创建人")
    private String createUser;
    //币种
    @NotNull
    private CurrencyEnum currency;
}
