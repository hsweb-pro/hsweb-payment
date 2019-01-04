package org.hswebframework.payment.api.merchant.event;

import org.hswebframework.payment.api.events.BusinessEvent;
import org.hswebframework.payment.api.merchant.Merchant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MerchantCreatedEvent implements BusinessEvent {
   private Merchant merchant;
}
