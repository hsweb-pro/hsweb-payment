package org.hswebframework.payment.api.enums;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;
import org.hswebframework.payment.api.payment.events.*;
import org.hswebframework.payment.api.payment.events.*;
import org.hswebframework.payment.api.payment.gateway.GateWayPaymentService;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.api.payment.quick.QuickPaymentService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

import java.util.function.Function;

/**
 * 交易类型
 *
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)
@Dict(id = "trans-type")
public enum TransType implements EnumDict<String> {
    /**
     * @see GateWayPaymentService
     */
    GATEWAY("网关支付", GatewayPaymentCompleteEvent::new),

    /**
     * @see QuickPaymentService
     */
    QUICK("快捷支付", QuickPaymentCompleteEvent::new),

    WITHHOLD("代扣", WithHoldPaymentCompleteEvent::new),

    SUBSTITUTE("代付", SubstitutePaymentCompleteEvent::new),

    WITHDRAW("提现", WithdrawPaymentCompleteEvent::new),

    CHARGE("服务费", PaymentCompleteEvent::new),

    CHANNEL_CHARGE("渠道服务费", PaymentCompleteEvent::new),

    AGENT_CHARGE("服务费", PaymentCompleteEvent::new),

    REFUND("退款", PaymentCompleteEvent::new),

    SUPPLEMENT("补登", PaymentCompleteEvent::new),

    SUPPLEMENT_ROLLBACK("补登回退", PaymentCompleteEvent::new);

    private String text;

    @Override
    public String getValue() {
        return this.name();
    }

    private Function<PaymentOrder, PaymentCompleteEvent> paymentDoneEventBuilder;

    public PaymentCompleteEvent createCompleteEvent(PaymentOrder order) {
        return paymentDoneEventBuilder.apply(order);
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
