package org.hswebframework.payment.test.channel;

import org.hswebframework.payment.api.payment.ChannelConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

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
