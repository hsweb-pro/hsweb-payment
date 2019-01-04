package org.hswebframework.payment.api.payment.substitute;

import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.PaymentChannel;
import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.payment.substitute.request.SubstituteRequest;
import org.hswebframework.payment.api.payment.substitute.response.SubstituteResponse;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface SubstituteChannel<P extends Payee,R extends SubstituteResponse>
        extends PaymentChannel<SubstituteRequest<P>, R> {

    @Override
    default TransType getTransType() {
        return TransType.SUBSTITUTE;
    }

    //支持的目标渠道,如:银行卡
    PayeeType getPayeeType();

    @Override
    default boolean match(SubstituteRequest<P> request) {
        //选择的目标渠道相同才支持
        return getPayeeType() == request.getPayeeType();
    }
}
