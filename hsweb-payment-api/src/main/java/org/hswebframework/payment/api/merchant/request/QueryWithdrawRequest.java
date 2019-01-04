package org.hswebframework.payment.api.merchant.request;

import org.hswebframework.payment.api.ApiRequest;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueryWithdrawRequest extends ApiRequest {

    @NotBlank
    private String merchantId;
}
