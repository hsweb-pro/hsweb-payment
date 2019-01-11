package org.hswebframework.payment.api.enums;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import org.hswebframework.payment.api.selector.ConfigSelectorRule;
import org.hswebframework.payment.api.selector.supports.RangeSelectorRule;
import org.hswebframework.payment.api.selector.supports.RangeSelectorRuleConfig;
import org.hswebframework.payment.api.selector.SelectorOption;
import org.hswebframework.payment.api.selector.SelectorRuleConfig;
import org.hswebframework.payment.api.selector.supports.RandomConfigSelectorRule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

import java.util.List;
import java.util.function.Supplier;

/**
 * 渠道选择规则
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@Dict(id = "selector-rule")
@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)
public enum SelectorRule implements EnumDict<String> {
    RANDOM("随机", SelectorRuleConfig.class, RandomConfigSelectorRule.class),
    RANGE("范围", RangeSelectorRuleConfig.class, RangeSelectorRule.class);

    private String text;

    private Class<? extends SelectorRuleConfig> configType;

    private Class<? extends ConfigSelectorRule> ruleType;

    @Override
    public String getValue() {
        return name();
    }

    @SuppressWarnings("all")
    public <T extends SelectorRuleConfig> T createConfig(String configString) {
        return (T) JSON.parseObject(configString, getConfigType());
    }

    @SneakyThrows
    public <C extends SelectorRuleConfig, O extends SelectorOption> ConfigSelectorRule<C, O> createRule(C config,
                                                                                                        Supplier<List<O>> allSupplier) {
        ConfigSelectorRule<C, O> rule = ruleType.newInstance();
        rule.setAllOptionSupplier(allSupplier);
        rule.setConfig(config);
        return rule;
    }
}
