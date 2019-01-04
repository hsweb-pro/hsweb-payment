package org.hswebframework.payment.merchant.openapi.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class GatewayPaymentRequest {

    @NotBlank(message = "请求ID[requestId]不能为空")
    @ApiModelProperty("全局请求ID")
    private String requestId;

    @NotBlank(message = "订单编号[orderId]不能为空")
    @ApiModelProperty("商户订单编号")
    private String orderId;

    @NotBlank(message = "支付渠道[channel]不能为空")
    @ApiModelProperty(value = "支付渠道标识", example = "alipay-h5")
    private String channel;

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

    @ApiModelProperty("异步通知地址")
    private String notifyUrl;

    @ApiModelProperty("支付完成返回地址")
    private String returnUrl;

    @ApiModelProperty("拓展参数")
    private String extraParam;

}
