package org.hswebframework.payment.api.merchant;

import org.hswebframework.payment.api.merchant.request.MerchantRegisterRequest;
import org.hswebframework.payment.api.merchant.request.MerchantUpdateRequest;
import org.hswebframework.payment.api.merchant.response.MerchantRegisterResponse;
import org.hswebframework.payment.api.merchant.response.MerchantUpdateResponse;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface MerchantService {

    String MASTER_MERCHANT_ID = "100000000001";

    Merchant getMerchantById(String id);

    Merchant getMerchantByUserId(String userId);

    /**
     * 注册商户
     *
     * @param request 注册请求
     * @return 注册结果
     */
    MerchantRegisterResponse registerMerchant(MerchantRegisterRequest request);


    MerchantUpdateResponse updateMerchant(MerchantUpdateRequest request);

}
