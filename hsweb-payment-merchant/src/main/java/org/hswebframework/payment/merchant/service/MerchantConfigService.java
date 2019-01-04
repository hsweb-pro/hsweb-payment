package org.hswebframework.payment.merchant.service;

import org.hswebframework.payment.merchant.entity.MerchantConfigEntity;
import org.hswebframework.web.service.CrudService;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface MerchantConfigService extends CrudService<MerchantConfigEntity, String> {

    MerchantConfigEntity selectByMerchantIdAndKey(String merchantId, String key);

}
