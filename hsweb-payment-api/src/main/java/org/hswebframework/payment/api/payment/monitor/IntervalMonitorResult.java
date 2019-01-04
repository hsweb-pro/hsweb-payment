package org.hswebframework.payment.api.payment.monitor;

import org.hswebframework.payment.api.enums.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.utils.time.DateFormatter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class IntervalMonitorResult {

    private TimeUnit.TimeInterval timeInterval;

    private long total;

    private String comment;

    public static IntervalMonitorResult of(TimeUnit.TimeInterval interval, long total, String comment) {
        IntervalMonitorResult result = new IntervalMonitorResult();
        result.setTimeInterval(interval);
        result.setTotal(total);
        result.setComment(comment);
        return result;
    }

    public static IntervalMonitorResult of(TimeUnit.TimeInterval interval, long total) {
        return of(interval, total, DateFormatter.toString(interval.getFrom(), "yyyy年MM月dd日")
                + "-" + DateFormatter.toString(interval.getTo(), "yyyy年MM月dd日"));
    }
}
