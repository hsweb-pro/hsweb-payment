package org.hswebframework.payment.merchant.entity;

import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.api.enums.BankCode;
import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.enums.WithdrawStatus;
import org.hswebframework.payment.api.enums.WithdrawType;
import org.hswebframework.payment.api.payment.payee.Payee;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@Table(name = "mer_withdraw")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantWithdrawEntity implements GenericEntity<String> {

    @Column(name = "id")
    @ApiModelProperty(value = "主键")
    private String id;

    @Column(name = "payment_id")
    @ApiModelProperty(value = "交易号")
    private String paymentId;

    @Column(name = "merchant_id")
    @ApiModelProperty(value = "商户ID")
    private String merchantId;

    @Column(name = "payee_info_json")
    private String payeeInfoJson;

    @Column(name = "payee_type")
    private PayeeType payeeType;

    @Column(name = "withdraw_type")
    @ApiModelProperty(value = "提现方式")
    private WithdrawType withdrawType;

    @Column(name = "trans_amount")
    @ApiModelProperty(value = "提现金额")
    private Long transAmount;

    @Column(name = "charge_amount")
    @ApiModelProperty(value = "提现手续费")
    private Long chargeAmount;

    @Column(name = "comment")
    @ApiModelProperty(value = "备注")
    private String comment;

    @Column(name = "status")
    @ApiModelProperty(value = "提现状态")
    private WithdrawStatus status;

    @Column(name = "apply_time")
    @ApiModelProperty(value = "提现申请时间")
    private Date applyTime;

    @Column(name = "handle_time")
    @ApiModelProperty(value = "提现处理时间")
    private Date handleTime;

    @Column(name = "complete_time")
    @ApiModelProperty(value = "提现完成时间")
    private Date completeTime;

    @Column(name = "close_time")
    @ApiModelProperty(value = "关闭时间")
    private Date closeTime;

    @Column(name = "handle_user")
    @ApiModelProperty(value = "处理人")
    private String handleUser;

    @Column(name = "complete_prove")
    @ApiModelProperty(value = "提现成功证明")
    private String completeProve;

    @Column(name = "merchant_name")
    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @Override
    @SneakyThrows
    public MerchantWithdrawEntity clone() {
        return (MerchantWithdrawEntity) super.clone();
    }

    public <P extends Payee> P getPayeeInfo() {
        if (StringUtils.isEmpty(getPayeeInfoJson())) {
            return null;
        }
        return (P) JSON.parseObject(getPayeeInfoJson(), payeeType.getPayeeType());
    }
}
