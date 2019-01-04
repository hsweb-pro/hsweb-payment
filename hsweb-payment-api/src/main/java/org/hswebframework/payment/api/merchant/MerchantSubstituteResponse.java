package org.hswebframework.payment.api.merchant;

import org.hswebframework.payment.api.ApiResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class MerchantSubstituteResponse extends ApiResponse {

    private String transId;
}
