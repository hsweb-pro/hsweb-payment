package org.hswebframework.payment.api.payment.supplement.request;

import org.hswebframework.payment.api.ApiRequest;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class SupplementCloseRequest extends ApiRequest {

    @NotBlank(message = "补登ID不能为空")
    private String supplementId;

    private String remark;

    public static SupplementCloseRequest of(String supplementId, String remark) {
        SupplementCloseRequest request = new SupplementCloseRequest();
        request.setRemark(remark);
        request.setSupplementId(supplementId);
        return request;
    }
}
