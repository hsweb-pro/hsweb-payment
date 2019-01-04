package org.hswebframework.payment.api.payment.substitute;

import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.payment.substitute.request.SubstituteRequest;
import org.hswebframework.payment.api.payment.substitute.response.SubstituteResponse;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface SubstitutePaymentService {

    <P extends Payee> SubstituteResponse
    requestSubstitute(SubstituteRequest<P> request);
}
