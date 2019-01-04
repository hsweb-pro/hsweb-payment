package org.hswebframework.payment.api.merchant.request;

import org.hswebframework.payment.api.ApiRequest;
import lombok.*;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawRequest extends ApiRequest {

    private Long transAmount;

    private String merchantId;

}
