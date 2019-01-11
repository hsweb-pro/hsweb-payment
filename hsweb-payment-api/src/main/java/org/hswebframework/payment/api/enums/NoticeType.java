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
@Dict(id = "notice-type")
public enum NoticeType implements EnumDict<String> {

    AGENT("AGENT","代理公告"),
    MERCHANT("MERCHANT","商户公告"),
    NORMAL("NORMAL", "普通公告"),
    URGENT("URGENT", "紧急公告");

    private String value;

    private String text;
}
