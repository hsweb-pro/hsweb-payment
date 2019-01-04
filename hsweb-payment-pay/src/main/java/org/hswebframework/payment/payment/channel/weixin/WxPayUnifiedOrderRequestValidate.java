package org.hswebframework.payment.payment.channel.weixin;

import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import org.hibernate.validator.constraints.NotBlank;

public class WxPayUnifiedOrderRequestValidate extends WxPayUnifiedOrderRequest {
    @Override
    @NotBlank(message = "请设置拓展参数[extraParam.spbillCreateIp]")
    public String getSpbillCreateIp() {
        return super.getSpbillCreateIp();
    }
}