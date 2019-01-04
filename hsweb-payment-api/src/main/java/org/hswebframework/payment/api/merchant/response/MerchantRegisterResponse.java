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
public class MerchantRegisterResponse extends ApiResponse {

    /**
     * 成功注册的商户信息,如果注册失败为<code>null</code>
     */
    private Merchant merchant;
}
