package org.hswebframework.payment.api.payment.bind.channel;

import org.hswebframework.payment.api.enums.BindCardPurpose;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface BindCardChannel {
    BindCardPurpose getPurpose();

    String getChannel();

    String getChannelName();

    ChannelBindCardResponse requestBindCard(ChannelBindCardRequest request);

    ChannelConfirmResponse confirmBindCard(ChannelConfirmRequest request);
}
