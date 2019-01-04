package org.hswebframework.payment.merchant.events;

import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantWithdrawLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class MerchantWithDrawRequestEvent {
    private Merchant merchant;

    private MerchantWithdrawLog withdrawLog;
}
