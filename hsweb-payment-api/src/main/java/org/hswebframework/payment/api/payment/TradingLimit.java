package org.hswebframework.payment.api.payment;

import org.hswebframework.payment.api.enums.TimeUnit;
import org.hswebframework.payment.api.utils.Money;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.payment.api.payment.events.PaymentLimitWarnEvent;
import org.hswebframework.payment.api.payment.events.PaymentOutOfLimitEvent;

import java.io.Serializable;

/**
 * 交易限额
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class TradingLimit implements Serializable {
    /**
     * 时间单位,如:单日,单月
     */
    private TimeUnit timeUnit;

    /**
     * 周期,如:1天,2个月
     */
    private int interval;

    /**
     * 限额,单位:分
     *
     * @see PaymentOutOfLimitEvent
     */
    private long limit;

    /**
     * 限额报警值,超过此限额将进行报警.
     *
     * @see PaymentLimitWarnEvent
     */
    private long warnLimit;

    @Override
    public String toString() {
        return interval + timeUnit.getText() + "限额:" + Money.cent(limit).toString();
    }
}
