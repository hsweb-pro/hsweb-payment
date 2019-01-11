package org.hswebframework.payment.api.settle.channel;

import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.enums.TransType;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 渠道下账请求
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelWithdrawRequest extends ApiRequest {

    @NotBlank(message = "渠道ID不能为空", groups = SettleValidateGroup.ForChannel.class)
    private String channelId;

    @NotBlank(message = "渠道不能为空", groups = SettleValidateGroup.ForChannel.class)
    private String channel;

    @NotBlank(message = "账户号不能为空", groups = SettleValidateGroup.ForAccount.class)
    private String accountNo;

    @NotBlank(message = "渠道名称不能为空", groups = SettleValidateGroup.ForAccount.class)
    private String channelName;

    @NotBlank(message = "渠道服务商ID不能为空", groups = SettleValidateGroup.ForAccount.class)
    private String channelProvider;

    @NotBlank(message = "渠道服务商名称不能为空", groups = SettleValidateGroup.ForAccount.class)
    private String channelProviderName;

    @NotBlank(message = "支付订单ID不能为空", groups = {SettleValidateGroup.ForChannel.class, SettleValidateGroup.ForAccount.class})
    private String paymentId;

    @NotBlank(message = "商户ID不能为空", groups = {SettleValidateGroup.ForChannel.class, SettleValidateGroup.ForAccount.class})
    private String merchantId;

    @NotBlank(message = "商户名称不能为空", groups = {SettleValidateGroup.ForChannel.class, SettleValidateGroup.ForAccount.class})
    private String merchantName;

    @NotNull(message = "交易类型不能为空", groups = {SettleValidateGroup.ForChannel.class, SettleValidateGroup.ForAccount.class})
    private TransType transType;

    //金额，分
    @Range(min = 1, message = "交易金额不能小于1", groups = {SettleValidateGroup.ForChannel.class, SettleValidateGroup.ForAccount.class})
    private long amount;

    private String memo;

}

