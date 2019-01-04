package org.hswebframework.payment.api.selector;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ConfigSelectorRule<C extends SelectorRuleConfig, O extends SelectorOption> {

    void setConfig(C config);

    void setAllOptionSupplier(Supplier<List<O>> optionSupplier);

    void setLimitFilter(BiPredicate<O,Long> filter);

    void setFilter(BiPredicate<O,Long> filter);

    O select(long amount);
}
