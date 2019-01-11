package org.hswebframework.payment.api.account.reqeust;

import lombok.*;
import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.enums.CurrencyEnum;
import org.hswebframework.payment.api.enums.TransType;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Lind
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AccountTransRequest extends ApiRequest {

    @ApiModelProperty("资金账号")
    private String accountNo;

    @ApiModelProperty("帐号名")
    private String accountName;

    @ApiModelProperty("交易金额")
    private Long transAmount;

    @ApiModelProperty("交易币种")
    private CurrencyEnum currency ;

    @ApiModelProperty("交易类型")
    private TransType transType;

    @ApiModelProperty("商户ID")
    private String merchantId;

    @ApiModelProperty("支付订单ID")
    private String paymentId;

    @ApiModelProperty("备注")
    private String comment;
}
