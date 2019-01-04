package org.hswebframework.payment.payment.events;

import org.hswebframework.payment.api.events.BusinessEvent;
import org.hswebframework.payment.api.payment.bind.BindCardRequest;
import org.hswebframework.payment.api.payment.bind.BindCardResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public class BindCardRequestSuccessEvent implements BusinessEvent {

    private BindCardRequest request;

    private BindCardResponse response;
}
