package org.hswebframework.payment.api.payment.payee;

import org.hswebframework.payment.api.enums.BankCode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class BankPayee extends Payee {

    //结算银行编号
    @NotNull(message = "银行编号不能为空")
    @ApiModelProperty(value = "银行编号", dataType = "options")
    private BankCode bankId;
    //结算银行账户
    @NotBlank(message = "银行卡号不能为空")
    @ApiModelProperty(value = "银行卡号")
    private String   accountNo;
    //银行账户名
    @NotBlank(message = "银行卡户名不能为空")
    @ApiModelProperty(value = "银行卡户名")
    private String   accountName;
    //账户类型
    @NotBlank(message = "账户类型不能为空")
    @ApiModelProperty(value = "账户类型")
    private String   accountType;
    //身份证号
    @NotBlank(message = "身份证号不能为空")
    @ApiModelProperty(value = "身份证号")
    private String   idNumber;
    //结算银行支行名
    @NotBlank(message = "银行支行名不能为空")
    @ApiModelProperty(value = "银行支行名")
    private String   branchName;
    //银行省份
    @NotBlank(message = "开户行省份不能为空")
    @ApiModelProperty(value = "开户省份")
    private String   province;
    //银行城市
    @NotBlank(message = "开户行城市不能为空")
    @ApiModelProperty(value = "开户城市")
    private String   city;

    @Override
    public String getPayee() {
        super.setPayee(getAccountNo());
        return getAccountNo();
    }

    @Override
    public String getPayeeName() {
        super.setPayeeName(getAccountName());
        return getAccountName();
    }
}
