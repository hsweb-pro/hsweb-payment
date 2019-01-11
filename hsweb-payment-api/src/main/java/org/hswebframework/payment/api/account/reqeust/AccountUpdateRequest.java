package org.hswebframework.payment.api.account.reqeust;

import lombok.EqualsAndHashCode;
import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.enums.AccountStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lind
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountUpdateRequest extends ApiRequest {

    @ApiModelProperty("资金账号")
    private String accountNo;

    @ApiModelProperty("账户状态")
    private AccountStatus status;
}
