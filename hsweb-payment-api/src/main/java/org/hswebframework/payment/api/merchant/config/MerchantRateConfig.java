package org.hswebframework.payment.api.merchant.config;

import org.hswebframework.payment.api.enums.TimeUnit;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.enums.TransRateType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * 商户的费率配置
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class MerchantRateConfig {

    /**
     * 交易类型
     */
    @NotNull(message = "交易类型不能为空")
    private TransType transType;

    /**
     * 渠道,如果为空则为该交易下通用
     */
//    @NotEmpty(message = "渠道不能为空")
    private String channel;

    private String channelName;

    //费率计算统计周期单位,默认单笔限额
    private TimeUnit chargeTimeUnit = TimeUnit.SINGLE;

    //费率计算周期
    private int chargeInterval = 1;

    /**
     * 费率类型
     */
    @NotNull(message = "费率类型不能为空")
    private TransRateType rateType;

    /**
     * 平台费率
     */
    @NotBlank(message = "费率不能为空")
    private String rate;

    /**
     * 上级代理费率类型
     */
    @Deprecated
    private TransRateType agentRateType;

    /**
     * 上级代理费率
     */
    @Deprecated
    private String agentRate;

    /**
     * 备注
     */
    private String memo;
}
