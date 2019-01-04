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
@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)
@Dict(id = "bind-card-status")
public enum BindCardStatus implements EnumDict<String> {

    binding("绑定中"),
    success("绑卡成功"),
    failed("绑卡失败"),
    ;
    private String text;

    @Override
    public String getValue() {
        return name();
    }
}
