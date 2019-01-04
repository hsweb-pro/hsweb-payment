package org.hswebframework.payment.api.merchant.config;

import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.payment.payee.Payee;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class MerchantPayeeConfig {

    @NotNull(message = "收款人类型不能为空")
    private PayeeType payeeType;

    @NotNull(message = "收款人信息不能为空")
    private String payeeInfoJson;

    @Valid
    public <P extends Payee> P getPayeeInfo() {
        return (P) JSON.parseObject(payeeInfoJson, payeeType.getPayeeType());
    }

}
