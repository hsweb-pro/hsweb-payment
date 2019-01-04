package org.hswebframework.payment.api.payment.supplement;

import org.hswebframework.payment.api.payment.supplement.request.SupplementCloseRequest;
import org.hswebframework.payment.api.payment.supplement.request.SupplementCompleteRequest;
import org.hswebframework.payment.api.payment.supplement.request.SupplementCreateRequest;
import org.hswebframework.payment.api.payment.supplement.request.SupplementRollbackRequest;
import org.hswebframework.payment.api.payment.supplement.response.SupplementCloseResponse;
import org.hswebframework.payment.api.payment.supplement.response.SupplementCompleteResponse;
import org.hswebframework.payment.api.payment.supplement.response.SupplementCreateResponse;
import org.hswebframework.payment.api.payment.supplement.response.SupplementRollbackResponse;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface SupplementService {

    SupplementCreateResponse create(SupplementCreateRequest request);

    SupplementCompleteResponse complete(SupplementCompleteRequest request);

    SupplementCloseResponse close(SupplementCloseRequest request);

    SupplementRollbackResponse rollback(SupplementRollbackRequest request);

}
