package org.hswebframework.payment.payment.channel.alipay.substitute;


import org.hswebframework.payment.payment.channel.alipay.AlipayConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.bean.ToString;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class AlipaySubstituteConfig extends AlipayConfig {
    // 商户appid
    @ApiModelProperty("AppId")
    @NotBlank
    private String appId;
    // 私钥pkcs8格式的
    @ApiModelProperty("私钥pkcs8格式的")
    @ToString.Ignore
    private String rsaPrivateKey;
    // 请求网关地址
    private String url = "https://mapi.alipay.com/gateway.do";
    // 支付宝公钥
    @ApiModelProperty("支付宝公钥")
    private String publicKey;

    @ApiModelProperty("应用公钥")
    private String appPublicKey;
    // RSA2
    @ApiModelProperty("签名类型")
    private String signType = "RSA2";

    @ApiModelProperty("付款帐号")
    private String email;

    @ApiModelProperty("付款账号名")
    private String accountName;

    @ApiModelProperty("付款账号类型")
    private String accountType;

    private String partnerId;


}
