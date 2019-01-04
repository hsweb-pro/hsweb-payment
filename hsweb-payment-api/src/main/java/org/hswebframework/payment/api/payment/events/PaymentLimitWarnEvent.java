package org.hswebframework.payment.api.payment.events;

import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.events.BusinessEvent;
import org.hswebframework.payment.api.payment.LimitScope;
import org.hswebframework.payment.api.payment.TradingLimit;
import lombok.*;

/**
 * 限额预警事件
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentLimitWarnEvent implements BusinessEvent {
    private LimitScope limitScope;

    private String merchantId;

    private TradingLimit limit;

    private TransType transType;

    private String channel;

    private long amount;
}
