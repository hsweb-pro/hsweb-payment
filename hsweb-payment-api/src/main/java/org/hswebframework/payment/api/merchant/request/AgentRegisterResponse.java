package org.hswebframework.payment.api.merchant.request;

import org.hswebframework.payment.api.ApiResponse;
import org.hswebframework.payment.api.merchant.AgentMerchant;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class AgentRegisterResponse extends ApiResponse {
    private AgentMerchant merchant;
}
