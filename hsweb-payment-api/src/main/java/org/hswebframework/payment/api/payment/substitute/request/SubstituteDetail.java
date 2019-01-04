package org.hswebframework.payment.api.payment.substitute.request;

import org.hswebframework.payment.api.payment.payee.Payee;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class SubstituteDetail<P extends Payee> {
    private String id;

    private String transNo;

    private P payee;

    private long amount;

    private String remark;
}
