package org.hswebframework.payment.api.enums;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@Dict(id = "payment-notify-type")
@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)
public enum NotifyType implements EnumDict<String> {
    NONE("NONE", "不通知"),
    HTTP("HTTP", "HTTP通知");

    private String value;

    private String text;

    @Override
    public boolean isWriteJSONObjectEnabled() {
        return false;
    }
}
