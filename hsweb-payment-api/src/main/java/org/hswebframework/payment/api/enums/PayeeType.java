package org.hswebframework.payment.api.enums;

import com.alibaba.fastjson.annotation.JSONType;
import org.hswebframework.payment.api.payment.payee.BankPayee;
import org.hswebframework.payment.api.payment.payee.Payee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

import java.util.Optional;

/**
 * 收款人类型,如:银行卡
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@Dict(id = "payee-type")
@JSONType(
        deserializer = EnumDict.EnumDictJSONDeserializer.class
)
public enum PayeeType implements EnumDict<String> {
    BANK("银行卡", BankPayee.class),
    ALIPAY("支付宝", Payee.class);

    private String text;

    private Class<? extends Payee> payeeType;

    @Override
    public String getValue() {
        return name();
    }

    public static Optional<PayeeType> of(String code) {
        return EnumDict.findByValue(PayeeType.class, code);
    }
}
