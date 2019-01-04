package org.hswebframework.payment.api.merchant;

import org.hswebframework.payment.api.merchant.payee.MerchantPayee;

/**
 * 商户收款人配置服务
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface MerchantPayeeService {

    MerchantPayee getMerchantPayee(String merchantId, String payeeId);

}
