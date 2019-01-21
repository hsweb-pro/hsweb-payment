package org.hswebframework.payment.payment.channel.weixin;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import org.hswebframework.payment.api.payment.ChannelConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.hswebframework.web.bean.ToString;
import org.springframework.util.StringUtils;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class WxPayChannelConfig extends ChannelConfig {
    //微信公众号或者小程序等的appid
    @ApiModelProperty(value = "AppId", required = true)
    private String appId;
    //微信支付商户号
    @ApiModelProperty(value = "商户号", required = true)
    private String mchId;

    //微信支付商户密钥
    @ApiModelProperty(value = "商户密钥", required = true)
    @ToString.Ignore
    private String mchKey;

    //服务商模式下的子商户公众账号ID
    @ApiModelProperty(value = "子商户AppId")
    private String subAppId;

    //服务商模式下的子商户号
    @ApiModelProperty(value = "子商户号")
    private String subMchId;

    @ApiModelProperty(value = "p12证书内容,base64格式")
    private String keyContentBase64;

    @ApiModelProperty(value = "代理Host")
    private String proxyHost;

    @ApiModelProperty(value = "代理端口")
    private int proxyPort;

    @ApiModelProperty(value = "代理用户名")
    private String proxyUsername;

    @ApiModelProperty(value = "代理密码")
    private String proxyPassword;

    public WxPayService createWxPayService() {
        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setAppId(this.getAppId());
        payConfig.setMchId(this.mchId);
        payConfig.setMchKey(this.mchKey);
        payConfig.setSubAppId(this.subAppId);
        payConfig.setSubMchId(this.subMchId);
        payConfig.setHttpProxyHost(this.proxyHost);
        payConfig.setHttpProxyPort(this.proxyPort);
        payConfig.setHttpProxyUsername(this.proxyUsername);
        payConfig.setHttpProxyPassword(this.proxyPassword);
        if (StringUtils.hasText(this.keyContentBase64)) {
            payConfig.setKeyContent(Base64.decodeBase64(this.keyContentBase64));
        }
        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);

        return wxPayService;
    }

}
