package org.hswebframework.payment.api.selector.supports;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.selector.SelectorOption;
import org.hswebframework.payment.api.selector.SelectorRuleConfig;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RandomConfigSelectorRule<O extends SelectorOption> extends AbstractConfigSelectorRule<SelectorRuleConfig, O> {
    private final static Random random = new Random();

    @Override
    public O select(long amount) {
        List<O> allConfig = getAllOption()
                .stream()
                .filter(c -> filter.test(c, amount))
                .collect(Collectors.toList());
        O config;
        if (allConfig.isEmpty()) {
            throw ErrorCode.CHANNEL_UNSUPPORTED.createException();
        }
        //判断限额
        allConfig = allConfig.stream()
                .filter(c -> limitFilter.test(c, amount))
                .collect(Collectors.toList());
        if (allConfig.isEmpty()) {
            throw ErrorCode.CHANEL_OUT_OF_LIMIT.createException();
        }

        if (allConfig.size() == 1) {
            config = allConfig.get(0);
        } else {
            config = allConfig.get(random.nextInt(allConfig.size()));
        }
        return doOnSelected(config);
    }
}
