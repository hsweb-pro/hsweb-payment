package org.hswebframework.payment.api.payment.withdraw;


import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.PaymentChannel;
import org.hswebframework.payment.api.payment.payee.Payee;

/**
 * @author Lind
 * @since 1.0
 */
public interface WithdrawPaymentChannel<RES extends WithdrawPaymentResponse, P extends Payee>
        extends PaymentChannel<WithdrawPaymentRequest<P>, RES> {

    @Override
    default TransType getTransType() {
        return TransType.WITHDRAW;
    }

    PayeeType getPayeeType();

    @Override
    default boolean match(WithdrawPaymentRequest<P> request) {
        return request.getPayeeType() == getPayeeType();
    }
}
