package org.hswebframework.payment.api.merchant.config;

import org.hswebframework.payment.api.enums.SelectorRule;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.TradingLimit;
import org.hswebframework.payment.api.selector.SelectorRuleConfig;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.payment.api.payment.ChannelConfig;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class MerchantChannelConfig {

    @NotNull(message = "交易类型不能为空")
    private TransType transType;

    @NotBlank(message = "渠道不能为空")
    private String channel;

    @NotBlank(message = "渠道名称不能为空")
    private String channelName;

    private String channelProvider;

    //商户的渠道结算账户
    private String settleAccountNo;

    /**
     * 商户渠道限额
     */
    private List<TradingLimit> tradingLimits;

    /**
     * 精确的交易限额配置,对渠道的单个配置进行限额
     * KEY为渠道ID: {@link ChannelConfig#id}
     * VALUE为对应渠道的限额配置
     *
     * @see ChannelConfig#id
     * @see this#getExactTradingLimit(String)
     */
    private Map<String, List<TradingLimit>> exactTradingLimits;

    /**
     * 渠道选择规则
     */
    private SelectorRule selectorRule = SelectorRule.RANDOM;

    /**
     * 规则对应的配置
     *
     * @see SelectorRule#getConfigType()
     */
    private String ruleConfig;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    public <T extends SelectorRuleConfig> T createRuleConfig() {
        return Optional.ofNullable(selectorRule)
                .map(rule -> rule.<T>createConfig(ruleConfig))
                .orElse(null);
    }

    public List<TradingLimit> getExactTradingLimit(String channelId) {
        return Optional.ofNullable(exactTradingLimits)
                .map(map -> map.get(channelId))
                .orElseGet(Collections::emptyList);
    }
}
