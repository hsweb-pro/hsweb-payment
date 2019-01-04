package org.hswebframework.payment.api.enums;

import org.junit.Test;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import static org.hswebframework.payment.api.enums.TransRateType.*;
import static org.junit.Assert.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class TransChargeTypeTest {

    @Test
    public void test() {
        //千分利
        assertEquals(PERCENT.calculate(100, "0.1").getCharge(), 0);
        assertEquals(PERCENT.calculate(100, "0.5").getCharge(), 1);
        assertEquals(PERCENT.calculate(100, "0.9").getCharge(), 1);

        //舍去厘
        assertEquals(PERCENT.calculate(500, "0.1").getCharge(), 1);
        assertEquals(PERCENT.calculate(500, "0.2").getCharge(), 1);
        assertEquals(PERCENT.calculate(500, "0.5").getCharge(), 3);
        assertEquals(PERCENT.calculate(500, "0.9").getCharge(), 5);

        assertEquals(PERCENT.calculate(5000, "0.1").getCharge(), 5);
        assertEquals(PERCENT.calculate(5000, "0.5").getCharge(), 25);
        assertEquals(PERCENT.calculate(5000, "0.9").getCharge(), 45);

        //百分利
        assertEquals(PERCENT.calculate(500, "1").getCharge(), 5);
        assertEquals(PERCENT.calculate(500, "5").getCharge(), 25);
        assertEquals(PERCENT.calculate(500, "9").getCharge(), 45);


        assertEquals(FIXED.calculate(100, "10").getCharge(), 10);
        assertEquals(FIXED.calculate(100, "555").getCharge(), 555);

        //阶梯收费
        String ladderRateConfig = "[" +
                "{\"type\":\"range\",\"values\":[0,100000],\"rateType\":\"FIXED\",\"rate\":1}" +
                ",{\"type\":\"range\",\"values\":[100000,500000],\"rateType\":\"PERCENT\",\"rate\":0.8}" +
                ",{\"type\":\"gt\",\"values\":[500000],\"rateType\":\"PERCENT\",\"rate\":0.7}" +
                "]";

        System.out.println(LADDER_SINGLE.getDescription(ladderRateConfig));
        System.out.println(LADDER_SINGLE.calculate(602000, ladderRateConfig));

        assertEquals(LADDER_SINGLE.calculate(100000, ladderRateConfig).getCharge(), 1);
        assertEquals(LADDER_SINGLE.calculate(500000, ladderRateConfig).getCharge(), 3201);
        assertEquals(LADDER_SINGLE.calculate(602000, ladderRateConfig).getCharge(), 3915);
    }
}