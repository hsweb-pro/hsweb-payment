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
public class SupplementCloseResponse extends ApiResponse {
    public static SupplementCloseResponse success() {
        SupplementCloseResponse response = new SupplementCloseResponse();
        response.setSuccess(true);
        return response;
    }
}
