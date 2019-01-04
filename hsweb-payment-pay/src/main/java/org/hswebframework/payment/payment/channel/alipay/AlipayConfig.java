package org.hswebframework.payment.payment.channel.alipay;

import org.hswebframework.payment.api.payment.ChannelConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.bean.ToString;

@Getter
@Setter
public class AlipayConfig extends ChannelConfig {
    // 商户appid
    @ApiModelProperty("AppId")
    @NotBlank
    private String appId;
    // 私钥pkcs8格式的
    @ApiModelProperty("私钥pkcs8格式的")
    @ToString.Ignore
    private String rsaPrivateKey;
    // 请求网关地址
    private String url = "https://openapi.alipay.com/gateway.do";
    // 支付宝公钥
    @ApiModelProperty("支付宝公钥")
    private String publicKey;

    @ApiModelProperty("应用公钥")
    private String appPublicKey;
    // RSA2
    @ApiModelProperty("签名类型")
    private String signType = "RSA2";

    @ApiModelProperty("代理Host")
    private String proxyHost;

    @ApiModelProperty("代理端口")
    private int proxyPort;
}