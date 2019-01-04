package org.hswebframework.payment.api.selector.supports;

import org.hswebframework.payment.api.selector.ConfigSelectorRule;
import org.hswebframework.payment.api.selector.SelectorOption;
import org.hswebframework.payment.api.selector.SelectorRuleConfig;
import lombok.Setter;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public abstract class AbstractConfigSelectorRule<C extends SelectorRuleConfig, O extends SelectorOption>
        implements ConfigSelectorRule<C, O> {

    protected C config;

    protected Supplier<List<O>> allOptionSupplier;

    @Setter
    protected BiPredicate<O,Long> filter = (o,a) -> true;

    @Setter
    protected BiPredicate<O,Long> limitFilter = (o,a) -> true;

    protected List<O> getAllOption() {
        return allOptionSupplier.get();
    }

    protected C getConfig() {
        return config;
    }

    public void doOnPresent(){

    }
    @Override
    public void setConfig(C config) {
        this.config = config;
    }

    @Override
    public void setAllOptionSupplier(Supplier<List<O>> optionSupplier) {
        this.allOptionSupplier = optionSupplier;
    }

}
