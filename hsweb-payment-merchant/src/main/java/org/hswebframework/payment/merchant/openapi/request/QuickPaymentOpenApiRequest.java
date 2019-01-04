package org.hswebframework.payment.merchant.openapi.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class QuickPaymentOpenApiRequest {
    @NotBlank(message = "请求ID[requestId]不能为空")
    @ApiModelProperty("全局请求ID")
    private String requestId;

    //绑卡流水号
    @ApiModelProperty("绑卡流水号")
    private String bindId;

    @NotBlank(message = "订单编号[orderId]不能为空")
    @ApiModelProperty("商户订单编号")
    private String orderId;

    @NotBlank(message = "商户编号[merchantId]不能为空")
    @ApiModelProperty("商户编号")
    private String merchantId;

    @NotBlank(message = "产品编号[productId]不能为空")
    @ApiModelProperty("产品编号")
    private String productId;

    @NotBlank(message = "产品名称[productName]不能为空")
    @ApiModelProperty("产品名称")
    private String productName;

    @Range(min = 0, max = Integer.MAX_VALUE, message = "金额必须大于0")
    @ApiModelProperty("金额(单位:分)")
    private long amount;

    @NotBlank(message = "银行卡户名[accountName]不能为空")
    private String accountName;

    @NotBlank(message = "银行卡号[accountNumber]不能为空")
    private String accountNumber;

    @NotBlank(message = "手机号码[phoneNumber]不能为空")
    private String phoneNumber;

    @NotBlank(message = "身份证号[idNumber]不能为空")
    private String idNumber;

    private String validDate;

    private String cvn2;

    private String remarks;
}
