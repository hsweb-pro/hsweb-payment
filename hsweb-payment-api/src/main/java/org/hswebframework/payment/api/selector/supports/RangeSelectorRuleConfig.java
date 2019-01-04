package org.hswebframework.payment.api.selector.supports;

import org.hswebframework.payment.api.selector.SelectorRuleConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class RangeSelectorRuleConfig extends SelectorRuleConfig {

    private List<String> configIdList;
}
