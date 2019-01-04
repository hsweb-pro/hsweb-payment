package org.hswebframework.payment.api.merchant.payee;

import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.payment.payee.Payee;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class MerchantPayee implements Serializable {
    private String id;

    @Valid
    private Payee payee;

    @NotNull(message = "收款人类型不能为空")
    private PayeeType payeeType;

    private String comment;
}
