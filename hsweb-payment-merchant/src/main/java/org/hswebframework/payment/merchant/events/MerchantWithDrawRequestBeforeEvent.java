package org.hswebframework.payment.merchant.events;

import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantWithdrawLog;
import org.hswebframework.payment.api.merchant.request.ApplyWithdrawRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MerchantWithDrawRequestBeforeEvent {
    private Merchant merchant;

    private ApplyWithdrawRequest request;
}
