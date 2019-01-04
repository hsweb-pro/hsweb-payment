package org.hswebframework.payment.account.dao.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hswebframework.payment.api.enums.AccountStatus;
import org.hswebframework.payment.api.enums.AccountType;
import org.hswebframework.payment.api.enums.CurrencyEnum;
import org.hswebframework.web.commons.entity.GenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * 资金账户
 *
 * @author Lind
 * @since 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table
@ApiModel(description = "资金账户")
@Getter
@Setter
public class AccountEntity implements GenericEntity<String> {

    @ApiModelProperty(value = "资金账号")
    @Column(name = "account_no")
    private String accountNo;

    @ApiModelProperty(value = "账户名称")
    @Column(name = "name")
    private String name;

    @ApiModelProperty(value = "商户ID")
    @Column(name = "merchant_id")
    private String merchantId;

    @ApiModelProperty(value = "资金账户类型")
    @Column(name = "type")
    private AccountType type;

    @ApiModelProperty(value = "资金账户状态")
    @Column(name = "status")
    private AccountStatus status;

    @ApiModelProperty(value = "余额")
    @Column(name = "balance")
    private Long balance;

    @ApiModelProperty(value = "冻结金额")
    @Column(name = "freeze_balance")
    private Long freezeBalance;

    @ApiModelProperty(value = "币种")
    @Column(name = "currency")
    private CurrencyEnum currency;

    @ApiModelProperty(value = "备注")
    @Column(name = "comment")
    private String comment;

    @Column(name = "id")
    @ApiModelProperty(value = "主键")
    private String id;

    @Column(name = "create_time")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "update_time")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Column(name = "create_user")
    @ApiModelProperty(value = "创建人")
    private String createUser;

    @Column(name = "update_user")
    @ApiModelProperty(value = "更新人")
    private String updateUser;

    public void initData(String createUser){
        this.createUser = createUser;
        this.updateUser = createUser;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public void updateData(String updateUser){
        this.updateUser = updateUser;
        this.updateTime = new Date();
    }

    public Long getTotalBalance(){
        return this.balance+this.freezeBalance;
    }
    @Override
    @SneakyThrows
    public AccountEntity clone() {
        return (AccountEntity) super.clone();
    }
}
