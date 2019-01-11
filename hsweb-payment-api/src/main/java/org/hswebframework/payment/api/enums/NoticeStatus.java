package org.hswebframework.payment.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@AllArgsConstructor
@Dict(id = "notice-status")
public enum NoticeStatus implements EnumDict<String> {

    CLOSE("CLOSE", "关闭"),
    OPEN("OPEN", "开启");

    private String value;

    private String text;
}
