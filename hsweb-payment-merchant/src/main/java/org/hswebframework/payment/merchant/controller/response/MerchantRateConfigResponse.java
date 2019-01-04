package org.hswebframework.payment.merchant.controller.response;

import lombok.Data;

/**
 * @author Lind
 * @since 1.0
 */
@Data
public class MerchantRateConfigResponse {


    /**
     * 交易类型
     */
    private String transType;

    /**
     * 渠道,如果为空则为该交易下通用
     */
//    @NotEmpty(message = "渠道不能为空")
    private String channel;

    private String channelName;
    /**
     * 费率类型
     */
    private String rateType;

    /**
     * 平台费率
     */
    private String rate;

    /**
     * 上级代理费率类型
     */
    private String agentRateType;

    /**
     * 上级代理费率
     */
    private String agentRate;

    /**
     * 备注
     */
    private String memo;
}
