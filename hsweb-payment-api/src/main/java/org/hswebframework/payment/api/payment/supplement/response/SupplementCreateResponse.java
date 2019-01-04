package org.hswebframework.payment.api.payment.supplement.response;

import org.hswebframework.payment.api.ApiResponse;
import org.hswebframework.payment.api.payment.supplement.Supplement;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class SupplementCreateResponse extends ApiResponse {

    private Supplement supplement;

    public static SupplementCreateResponse of(Supplement supplement) {
        SupplementCreateResponse response = new SupplementCreateResponse();
        response.setSuccess(true);
        response.setSupplement(supplement);
        return response;
    }
}
