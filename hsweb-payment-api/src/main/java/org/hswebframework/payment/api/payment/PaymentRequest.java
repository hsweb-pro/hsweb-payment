package org.hswebframework.payment.api.payment;

import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.enums.NotifyType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class PaymentRequest extends ApiRequest {

    private String paymentId;

    @NotBlank(message = "订单编号[orderId]不能为空")
    private String orderId;

    @NotBlank(message = "支付渠道[channel]不能为空")
    private String channel;

    private String channelId;

    @NotBlank(message = "商户编号[merchantId]不能为空")
    private String merchantId;

    @NotBlank(message = "商户名称[merchantName]不能为空")
    private String merchantName;

    @NotBlank(message = "产品编号[productId]不能为空")
    private String productId;

    @NotBlank(message = "产品名称[productName]不能为空")
    private String productName;

    @Range(min = 0, max = Integer.MAX_VALUE, message = "金额必须大于0")
    private long amount;

    @NotBlank(message = "币种[currency]不能为空")
    private String currency = Currency.getInstance(Locale.CHINA).getCurrencyCode();

    @NotNull(message = "通知类型[notifyType]不能为空")
    private NotifyType notifyType;

    private Map<String, Object> notifyConfig;

    /**
     * 渠道拓展参数
     */
    private Map<String, String> extraParam;

    private String returnUrl;

    public Optional<String> getExtraParameter(String key) {
        return Optional.ofNullable(extraParam)
                .map(map -> map.get(key));
    }
}
