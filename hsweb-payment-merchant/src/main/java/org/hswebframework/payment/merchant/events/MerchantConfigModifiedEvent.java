package org.hswebframework.payment.merchant.events;

import org.hswebframework.payment.merchant.entity.MerchantConfigEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public class MerchantConfigModifiedEvent {
    private MerchantConfigEntity configEntity;
}
