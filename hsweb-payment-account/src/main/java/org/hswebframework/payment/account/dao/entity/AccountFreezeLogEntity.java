package org.hswebframework.payment.account.dao.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hswebframework.payment.api.enums.FreezeDirection;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.web.commons.entity.GenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table
@ApiModel(description = "资金账户")
@Getter
@Setter
public class AccountFreezeLogEntity implements GenericEntity<String> {

    @Column(name = "id")
    @ApiModelProperty(value = "主键")
    private String id;

    @Column(name = "payment_id")
    @ApiModelProperty(value = "交易ID")
    private String paymentId;

    @Column(name = "amount")
    @ApiModelProperty(value = "冻结/解冻金额")
    private Long amount;

    @Column(name = "account_no")
    @ApiModelProperty(value = "资金账号")
    private String accountNo;

    @Column(name = "direction")
    @ApiModelProperty(value = "当前冻结/解冻方向")
    private FreezeDirection direction;

    @Column(name = "freeze_time")
    @ApiModelProperty(value = "冻结时间")
    private Date freezeTime;

    @Column(name = "unfreeze_time")
    @ApiModelProperty(value = "解冻时间")
    private Date unfreezeTime;

    @Column(name = "comment")
    @ApiModelProperty(value = "备注")
    private String comment;

    @Column(name = "trans_type")
    private TransType transType;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "unfreeze_comment")
    private String unfreezeComment;
    @Override
    @SneakyThrows
    public AccountFreezeLogEntity clone() {
        return (AccountFreezeLogEntity)super.clone();
    }

}


