package org.hswebframework.payment.api.merchant.config;

import org.hswebframework.web.dict.defaults.TrueOrFalse;
import org.junit.Assert;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class StringSourceMerchantConfigHolderTest {

    @org.junit.Test
    public void test() {
        MerchantConfigHolder intHolder = new StringSourceMerchantConfigHolder("100");
        MerchantConfigHolder enumHolder = new StringSourceMerchantConfigHolder("true");
        MerchantConfigHolder objectHolder = new StringSourceMerchantConfigHolder("{\"name\":\"1234\",\"age\":100}");
        Assert.assertEquals(intHolder.asInt().orElse(0).intValue(), 100);
        Assert.assertEquals(enumHolder.as(TrueOrFalse.class).orElse(TrueOrFalse.FALSE), TrueOrFalse.TRUE);
        Assert.assertEquals(objectHolder.as(Test.class).orElse(new Test()).getName(), "1234");
    }
}