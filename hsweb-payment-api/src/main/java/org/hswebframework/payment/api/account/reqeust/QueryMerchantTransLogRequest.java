package org.hswebframework.payment.api.account.reqeust;

import lombok.EqualsAndHashCode;
import org.hswebframework.payment.api.ApiRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryMerchantTransLogRequest extends ApiRequest {

    @ApiModelProperty("账户状态")
    private String merchantId;

    @ApiModelProperty("支付订单号")
    private String paymentId;


}
