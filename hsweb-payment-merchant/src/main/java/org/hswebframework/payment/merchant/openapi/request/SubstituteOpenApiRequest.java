package org.hswebframework.payment.merchant.openapi.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class SubstituteOpenApiRequest {

    @NotBlank(message = "商户Id[merchantId]不能为空")
    private String merchantId;

    @NotBlank(message = "收款方式[payeeType]不能为空")
    private String payeeType;

    @NotBlank(message = "代付明细[details]不能为空")
    private String details;

    @NotBlank(message = "商户订单ID[orderId]不能为空")
    private String orderId;

    @NotBlank(message = "交易流水号[transNo]不能为空")
    private String transNo;

    private String notifyUrl;

    private String remark;


}
