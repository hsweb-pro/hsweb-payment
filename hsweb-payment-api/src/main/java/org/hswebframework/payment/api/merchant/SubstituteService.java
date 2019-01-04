package org.hswebframework.payment.api.merchant;

import org.hswebframework.payment.api.merchant.request.MerchantSubstituteRequest;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface SubstituteService {

    MerchantSubstituteResponse requestSubstitute(MerchantSubstituteRequest request);
}
