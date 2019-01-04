package org.hswebframework.payment.api.payment.supplement.response;

import org.hswebframework.payment.api.ApiResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class SupplementRollbackResponse extends ApiResponse {
    public static SupplementRollbackResponse success() {
        SupplementRollbackResponse response = new SupplementRollbackResponse();
        response.setSuccess(true);
        return response;
    }
}
