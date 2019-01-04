package org.hswebframework.payment.merchant.service;

import org.hswebframework.payment.api.merchant.config.MerchantServiceConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "hsweb.pay.merchant")
@Getter
@Setter
public class MerchantProperties {

    private boolean enableTwoFactor = true;

    private String twoFactorDomain = "hsweb.pro";

    //是否开启邮件通知
    private boolean twoFactorEmailNotify = false;
    private String  twoFactorEmailFrom;
    private String  twoFactorEmailSender = "default";
    //默认10分钟过期
    private long    twoFactorExpireTime  = 10 * 60 * 1000L;

    private List<String> defaultService = new ArrayList<>();


    public List<MerchantServiceConfig> getDefaultServiceConfig() {
        return defaultService.stream()
                .map(MerchantServiceConfig::ofExpress)
                .collect(Collectors.toList());
    }
}
