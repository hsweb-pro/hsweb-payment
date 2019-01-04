package org.hswebframework.payment.api.merchant.response;

import org.hswebframework.payment.api.ApiResponse;
import org.hswebframework.payment.api.merchant.Merchant;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class MerchantUpdateResponse extends ApiResponse {

    private Merchant merchant;
}
