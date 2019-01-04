package org.hswebframework.payment.merchant.controller.response;

import org.hswebframework.payment.api.merchant.config.MerchantRateConfig;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MerchantRateAndChannelConfig extends MerchantRateConfig {

    private boolean isEnable;
}
