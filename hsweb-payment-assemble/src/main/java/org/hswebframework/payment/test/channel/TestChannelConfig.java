package org.hswebframework.payment.test.channel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.payment.api.payment.ChannelConfig;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class TestChannelConfig extends ChannelConfig {
    @ApiModelProperty("失败率")
    private int failureRate;

}
