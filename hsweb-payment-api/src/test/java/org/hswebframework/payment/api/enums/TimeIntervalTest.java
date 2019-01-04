package org.hswebframework.payment.api.enums;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class TimeIntervalTest {

    public static void main(String[] args) {
        System.out.println(TimeUnit.DAY.getBeforeNowInterval(1));
        System.out.println(TimeUnit.WEEK.getBeforeNowInterval(1));
        System.out.println(TimeUnit.WEEK_OF_MONDAY.getBeforeNowInterval(1));

        System.out.println(TimeUnit.MONTH.getBeforeNowInterval(1));

    }

}