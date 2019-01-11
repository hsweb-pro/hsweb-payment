package org.hswebframework.payment.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * 代付明细状态
 *
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
@Dict(id = "substitute-detail-status")
public enum SubstituteDetailStatus implements EnumDict<String> {
    PROCESSING("处理中"),
    SUCCESS("成功"),
    FAIL("失败");

    private String text;

    @Override
    public String getValue() {
        return name();
    }
}
