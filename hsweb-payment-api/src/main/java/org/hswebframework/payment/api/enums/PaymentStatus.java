package org.hswebframework.payment.api.enums;

import com.alibaba.fastjson.JSONObject;
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
@Dict(id = "payment-status")
@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)
public enum PaymentStatus implements EnumDict<String> {
    prepare("发起支付请求"),
    paying("支付中"),
    success("支付成功"),
    fail("支付失败"),
    timeout("支付超时"),
    requestFail("发起支付申请失败");

    private String text;

    @Override
    public String getValue() {
        return name();
    }

    @Override
    public boolean isWriteJSONObjectEnabled() {
        return true;
    }

    @Override
    public Object getWriteJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", getValue());
        jsonObject.put("text", getText());
        return jsonObject;
    }
}
