package org.hswebframework.payment.api.enums;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 交易费率
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@Dict(id = "trans-rate-type")
@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)
@Slf4j
public enum TransRateType implements EnumDict<String> {


    FIXED("固定费率") {
        @Override
        public TransCharge calculate(long amount, Supplier<Long> sumAmount, String rate) {
            TransCharge transCharge = new TransCharge();
            transCharge.setCharge(Long.valueOf(rate));
            transCharge.setFormat("固定收取:%s元");
            return transCharge;
        }

        @Override
        public String getDescription(String rate) {
            return "固定收取:" + TransCharge.format(Long.valueOf(rate));
        }
    },

    PERCENT("百分比(%)") {
        @Override
        public TransCharge calculate(long amount, Supplier<Long> sumAmount, String rate) {
            BigDecimal percent = new BigDecimal(rate);
            BigDecimal amountDecimal = new BigDecimal(amount);
            // amount*rate/100
            return TransCharge.of(amountDecimal
                    .multiply(percent)
                    .divide(new BigDecimal(100), RoundingMode.HALF_UP)
                    .setScale(0, BigDecimal.ROUND_HALF_UP) //四舍五入
                    .longValue(), "按[" + rate + "%%]收取:%s元");
        }

        @Override
        public String getDescription(String rate) {
            return "按" + rate + "%";
        }
    },
    /**
     * 阶梯收费,费率配置格式:
     * <pre class='code json'>
     * [
     * {"type":"range", "values":[0,10000] ,"rateType":"RANGE","rate":"1"},
     * {"type":"range","values":[10000,100000],"type":"PERCENT","rate":"0.8"},
     * {"type":"gt","values":[100000],"type":"PERCENT","rate":"0.7"}
     * ]
     * </pre>
     *
     * @see Ladder
     * @see LadderType
     */
    LADDER_SINGLE("阶梯单笔收费") {
        @Override
        public TransCharge calculate(long amount, Supplier<Long> sumAmount, String rate) {
            //  2018/11/15 阶梯费率的计算需不需要减去上一个阶梯,比如0-1000收取2%,1000-10000收取1%,如果金额是5000.那是收5000的1%呢,还是收4000的1%+1000的2%

            List<Ladder> ladders = JSON.parseArray(rate, Ladder.class);
            Collections.sort(ladders);
            BigDecimal charge = new BigDecimal(0);
            //剩余资金
            long lastAmount = amount;
            StringBuilder format = new StringBuilder();

            long tempAmount = amount;
            all:
            while (lastAmount > 0) {
                boolean anyMatch = false;
                for (Ladder ladder : ladders) {
                    if (ladder.match(lastAmount)) {
                        anyMatch = true;
                        long diff = ladder.getMin() == 0 ? 0 : Math.abs(lastAmount - ladder.getMin());
                        if (diff == 0) {
                            diff = lastAmount;
                        }
                        lastAmount -= diff;
                        TransCharge transCharge = ladder.calculate(diff);
                        format.append("金额[")
                                .append(TransCharge.format(lastAmount))
                                .append("-")
                                .append(TransCharge.format(tempAmount))
                                .append("]部分:")
                                .append(transCharge.toString().replace("%", "%%"))
                                .append("\n");
                        charge = charge.add(new BigDecimal(transCharge.getCharge()));
                        tempAmount = lastAmount;
                        if (lastAmount <= 0 || diff <= 0) {
                            break all;
                        }
                    }
                }
                if (!anyMatch) {
                    log.warn("未匹配到阶梯规则.totalAmount:{}, amount:{},rate:\n{}",
                            amount,
                            lastAmount,
                            rate);
                    break;
                }
            }
            format.append("总计收费:%s元");
            TransCharge transCharge = new TransCharge();
            transCharge.setCharge(charge.longValue());
            transCharge.setFormat(format.toString());

            return transCharge;
        }

        @Override
        public String getDescription(String rate) {
            List<Ladder> ladders = JSON.parseArray(rate, Ladder.class);
            StringBuilder builder = new StringBuilder();
            for (Ladder ladder : ladders) {
                builder.append(ladder.getType().getExplain(ladder.values))
                        .append(",")
                        .append(ladder.getRateType().getDescription(ladder.rate))
                        .append("\n");
            }
            return builder.toString();
        }
    },
    LADDER("阶梯收费") {
        @Override
        public TransCharge calculate(long amount, Supplier<Long> sumAmount, String rate) {
            List<Ladder> ladders = JSON.parseArray(rate, Ladder.class);
            Collections.sort(ladders);
            long sum = sumAmount.get();
            for (Ladder ladder : ladders) {
                if (ladder.match(sum)) {
                    return ladder.calculate(amount);
                }
            }
            throw ErrorCode.BUSINESS_FAILED.createException("费率配置错误!");
        }

        @Override
        public String getDescription(String rate) {
            List<Ladder> ladders = JSON.parseArray(rate, Ladder.class);
            StringBuilder builder = new StringBuilder();
            for (Ladder ladder : ladders) {
                builder.append(ladder.getType().getExplain(ladder.values))
                        .append(",")
                        .append(ladder.getRateType().getDescription(ladder.rate))
                        .append("\n");
            }
            return builder.toString();
        }
    };

    private String text;

    public abstract String getDescription(String rate);

    @Override
    public String getValue() {
        return name();
    }

    public TransCharge calculate(long amount, String rate) {
        return calculate(amount, () -> amount, rate);
    }

    public abstract TransCharge calculate(long amount, Supplier<Long> sumAmount, String rate);

    /**
     * 交易收费详情
     */
    @Getter
    @Setter
    public static class TransCharge {
        public static TransCharge none = new TransCharge() {
            @Override
            public String toString() {
                return "不收费";
            }
        };

        static NumberFormat nf = new DecimalFormat("#,###.####");

        private String format = "";

        private long charge = 0;

        public static String format(long cent) {
            return nf.format(new BigDecimal(cent)
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .divide(new BigDecimal(100d), BigDecimal.ROUND_HALF_UP));
        }

        public static TransCharge of(long charge, String format) {
            TransCharge transCharge = new TransCharge();

            transCharge.setFormat(format);
            transCharge.setCharge(charge);
            return transCharge;
        }

        public String toString(String currency) {
            Currency curr = Currency.getInstance(currency);
            return toString((charge) -> String.format("%s%s",
                    curr.getSymbol(Locale.CHINA),
                    format(charge)));

        }

        public String toString(Function<Long, String> formatter) {
            return String.format(format, formatter.apply(charge));
        }

        public void ifPresent(Consumer<TransCharge> chargeConsumer) {
            if (charge > 0) {
                chargeConsumer.accept(this);
            }
        }

        @Override
        public String toString() {
            return toString(TransCharge::format);
        }
    }

    /**
     * 阶梯收费配置
     */
    @Getter
    @Setter
    public static class Ladder implements Comparable<Ladder> {
        LadderType type;

        long[] values;

        TransRateType rateType;

        String rate;

        long getMin() {
            if (values.length == 2) {
                return Math.min(values[0], values[1]);
            }
            if (type == LadderType.lt) {
                return 0;
            }
            return values[0];
        }

        long getMax() {
            if (values.length == 2) {
                return Math.max(values[0], values[1]);
            }
            return values[0];
        }

        long getDiff(long value) {
            if (values.length == 2) {
                return value - Math.min(values[0], values[1]);
            }
            return Math.abs(values[0] - value);
        }

        public boolean match(long amount) {
            return type.match(values, amount);
        }

        public TransCharge calculate(long amount) {
            TransCharge transCharge = rateType.calculate(amount, rate);
            transCharge.setFormat(type.getExplain(values) + "," + transCharge.getFormat());
            return transCharge;
        }

        @Override
        public int compareTo(Ladder o) {
            return Long.compare(o.getMax(), getMax());
        }
    }

    /**
     * 阶梯类型
     */
    public enum LadderType {
        range {
            @Override
            boolean match(long[] conf, long value) {
                return conf.length == 2 && value > conf[0] && value <= conf[1];
            }

            @Override
            String getExplain(long[] conf) {
                return String.format("交易额在[%s]至[%s]之间", TransCharge.format(conf[0]), TransCharge.format(conf[1]));
            }
        }, gt {
            @Override
            boolean match(long[] conf, long value) {
                return conf.length == 1 && value > conf[0];
            }

            @Override
            String getExplain(long[] conf) {
                return String.format("交易额大于[%s]", TransCharge.format(conf[0]));
            }
        }, lt {
            @Override
            boolean match(long[] conf, long value) {
                return conf.length == 1 && value < conf[0];
            }

            @Override
            String getExplain(long[] conf) {
                return String.format("交易额小于[%s]", TransCharge.format(conf[0]));
            }
        }, is {
            @Override
            boolean match(long[] conf, long value) {
                return conf.length == 1 && value == conf[0];
            }

            @Override
            String getExplain(long[] conf) {
                return String.format("交易额等于[%s]", TransCharge.format(conf[0]));
            }
        };

        abstract boolean match(long[] conf, long value);

        abstract String getExplain(long[] conf);
    }

}
