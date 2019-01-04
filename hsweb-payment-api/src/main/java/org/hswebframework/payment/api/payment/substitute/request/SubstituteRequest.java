package org.hswebframework.payment.api.payment.substitute.request;

import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.payee.Payee;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class SubstituteRequest<P extends Payee> extends PaymentRequest {

    private PayeeType payeeType;

    private List<SubstituteDetail<P>> details;

    private String remark;
}
