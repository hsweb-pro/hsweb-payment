package org.hswebframework.payment.api.payment.withdraw;

import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.payee.Payee;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 代付请求
 *
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawPaymentRequest<P extends Payee> extends PaymentRequest {

    private PayeeType payeeType;

    private P payee;

    private String remark;


}

