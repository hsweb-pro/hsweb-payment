package org.hswebframework.payment.payment.channel.alipay.withdraw;


import org.hswebframework.payment.payment.channel.alipay.AlipayConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class AlipayWithdrawConfig extends AlipayConfig {

    @ApiModelProperty("付款帐号")
    private String email;

    @ApiModelProperty("付款方姓名")
    private String payerShowName;

    @ApiModelProperty("付款方真实姓名")
    private String payerRealName;


}
