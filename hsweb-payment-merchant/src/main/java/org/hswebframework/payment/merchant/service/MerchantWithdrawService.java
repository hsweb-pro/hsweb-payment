package org.hswebframework.payment.merchant.service;

import org.hswebframework.payment.merchant.entity.MerchantWithdrawEntity;
import org.hswebframework.web.service.CrudService;

/**
 * @author Lind
 * @since 1.0
 */
public interface MerchantWithdrawService extends CrudService<MerchantWithdrawEntity,String> {

    MerchantWithdrawEntity queryWithdrawLogByIdAndMerchantId(String id,String merchantId);
}
