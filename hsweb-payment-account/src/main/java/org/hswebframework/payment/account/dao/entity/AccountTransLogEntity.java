package org.hswebframework.payment.account.dao.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hswebframework.payment.api.enums.AccountTransType;
import org.hswebframework.payment.api.enums.CurrencyEnum;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.web.commons.entity.GenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table
@ApiModel(description = "资金账户交易记录")
public class AccountTransLogEntity implements GenericEntity<String> {

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "currency")
    private CurrencyEnum currency;

    @ApiModelProperty(value = "交易类型")
    @Column(name = "trans_type")
    private TransType transType;

    @ApiModelProperty(value = "交易金额")
    @Column(name = "trans_amount")
    private Long transAmount;

    @ApiModelProperty(value = "交易发生后，资金账户余额")
    @Column(name = "balance")
    private Long balance;

    @Column(name = "status")
    private Integer status;

    @ApiModelProperty(value = "备注")
    @Column(name = "comment")
    private String comment;

    @ApiModelProperty(value = "账户交易类型")
    @Column(name = "account_trans_type")
    private AccountTransType accountTransType;

    @Column(name = "id")
    @ApiModelProperty(value = "主键")
    private String id;

    @Column(name = "create_time")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "create_user")
    @ApiModelProperty(value = "创建人")
    private String createUser;

    @Column(name = "account_name")
    @ApiModelProperty(value = "户名")
    private String accountName;

    public void initData(String createUser) {
        this.createUser = createUser;
        this.createTime = new Date();
    }

    @Override
    @SneakyThrows
    public AccountEntity clone() {
        return (AccountEntity) super.clone();
    }
}
