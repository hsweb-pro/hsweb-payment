package org.hswebframework.payment.api.selector.supports;

import org.hswebframework.payment.api.selector.SelectorOption;

import java.util.function.BiPredicate;
import java.util.function.Consumer;


/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RangeSelectorRule<O extends SelectorOption> extends AbstractConfigSelectorRule<RangeSelectorRuleConfig, O> {

    private RandomConfigSelectorRule<O> roundRule = new RandomConfigSelectorRule<>();

    public RangeSelectorRule() {
        roundRule.setFilter((o, r) -> getConfig().getConfigIdList().contains(o.getId()) && filter.test(o, r));
        roundRule.setAllOptionSupplier(() -> allOptionSupplier.get());
    }

    @Override
    public void setLimitFilter(BiPredicate<O, Long> limitFilter) {
        roundRule.setLimitFilter(limitFilter);
        super.setLimitFilter(limitFilter);
    }

    @Override
    public void onSelected(Consumer<O> selectedConsumer) {
        super.onSelected(selectedConsumer);
        roundRule.onSelected(selectedConsumer);
    }
    @Override
    public O select(long amount) {
        return roundRule.select(amount);
    }
}
