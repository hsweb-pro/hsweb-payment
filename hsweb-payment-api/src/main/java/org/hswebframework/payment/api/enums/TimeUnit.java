package org.hswebframework.payment.api.enums;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)
@Dict(id = "time-unit")
public enum TimeUnit implements EnumDict<String> {
    SINGLE("笔", Function.identity(), (d, i) -> d),
    DAY("天", Function.identity(), DateTime::plusDays),
    WEEK("周", Function.identity(), DateTime::plusWeeks),
    WEEK_OF_MONDAY("周(从周一开始)", time -> time.withDayOfWeek(1), DateTime::plusWeeks),
    MONTH("月", Function.identity(), DateTime::plusMonths),
    YEAR("年", Function.identity(), DateTime::plusYears),;
    @Getter
    private String text;

    @Override
    public String getValue() {
        return name();
    }

    private Function<DateTime, DateTime> fromConvert;

    private BiFunction<DateTime, Integer, DateTime> defaultConvert;


    /**
     * 获取时间间隔信息,如获取 2018-10-10
     *
     * @param from      从什么时候开始
     * @param direction 方向,向前还是向后
     * @param interval  数量是多少
     * @return 时间间隔
     */
    public TimeInterval getInterval(Date from, Direction direction, int interval) {

        //设置为明天的0时0分0秒
        DateTime newFrom = fromConvert.apply(new DateTime(from).plusDays(1).withMillisOfDay(0));
        DateTime to = defaultConvert.apply(newFrom.toDateTime(), direction.getNumber(interval));
        //如果from在to之后则交换顺序
        if (newFrom.isAfter(to)) {
            DateTime tmp = newFrom;
            newFrom = to;
            to = tmp;
        }
        return TimeInterval.of(newFrom.toDate(), to.toDate());
    }

    public TimeInterval getBeforeInterval(Date from, int interval) {
        return getInterval(from, Direction.BEFORE, interval);
    }

    public TimeInterval getAfterInterval(Date from, int interval) {
        return getInterval(from, Direction.AFTER, interval);
    }

    public TimeInterval getBeforeNowInterval(int interval) {
        return getInterval(new Date(), Direction.BEFORE, interval);
    }

    public TimeInterval getAfterNowInterval(int interval) {
        return getInterval(new Date(), Direction.AFTER, interval);
    }

    public enum Direction {
        BEFORE {
            @Override
            public int getNumber(int interval) {
                return -interval;
            }
        }, AFTER {
            @Override
            public int getNumber(int interval) {
                return interval;
            }
        };

        public abstract int getNumber(int interval);
    }

    @Getter
    @Setter
    public static class TimeInterval {
        private Date from;

        private Date to;

        static TimeInterval of(Date from, Date to) {
            TimeInterval intervalInfo = new TimeInterval();
            intervalInfo.from = from;
            intervalInfo.to = to;
            return intervalInfo;
        }

        public boolean in(Date date) {
            return date.getTime() >= from.getTime() && date.getTime() <= to.getTime();
        }

        @Override
        public String toString() {
            return "从" + DateFormatter.toString(from, "yyyy-MM-dd") +
                    "到" + DateFormatter.toString(to, "yyyy-MM-dd");
        }
    }
}
