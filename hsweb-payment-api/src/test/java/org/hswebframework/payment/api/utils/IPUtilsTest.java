package org.hswebframework.payment.api.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class IPUtilsTest {

    @Test
    public void test(){

        Assert.assertEquals(IPUtils.getRealIp("127.0.0.1"),"127.0.0.1");

        Assert.assertEquals(IPUtils.getRealIp("localhost, 127.0.0.1"),"127.0.0.1");

        Assert.assertEquals(IPUtils.getRealIp(""),"");
    }

}