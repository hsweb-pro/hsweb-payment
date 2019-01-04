package org.hswebframework.payment.api.merchant;

import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.enums.WithdrawStatus;
import org.hswebframework.payment.api.enums.WithdrawType;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantWithdrawLog {


    private String id;

    @ApiModelProperty(value = "交易号")
    private String paymentId;

    @ApiModelProperty(value = "商户ID")
    private String merchantId;

    @ApiModelProperty(value = "收款类型")
    private PayeeType payeeType;

    @ApiModelProperty(value = "提现方式")
    private WithdrawType withdrawType;

    @ApiModelProperty(value = "提现金额")
    private Long transAmount;

    @ApiModelProperty(value = "提现手续费")
    private Long chargeAmount;

    @ApiModelProperty(value = "备注")
    private String comment;

    @ApiModelProperty(value = "提现状态")
    private WithdrawStatus status;

    @ApiModelProperty(value = "提现申请时间")
    private Date applyTime;

    @ApiModelProperty(value = "提现处理时间")
    private Date handleTime;

    @ApiModelProperty(value = "提现完成时间")
    private Date completeTime;

    @ApiModelProperty(value = "关闭时间")
    private Date closeTime;

    @ApiModelProperty(value = "处理人")
    private String handleUser;

    @ApiModelProperty(value = "提现成功证明")
    private String completeProve;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;


}
