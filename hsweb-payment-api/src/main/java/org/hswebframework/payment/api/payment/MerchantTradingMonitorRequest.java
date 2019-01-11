package org.hswebframework.payment.api.payment;

import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.enums.TimeUnit;
import org.hswebframework.payment.api.enums.TransType;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantTradingMonitorRequest implements Serializable {
    private String merchantId;
    private TransType transType;
    private String channel;
    private String channelProvider;
    private String channelId;
    private String agentId;

    private PaymentStatus status;

    private List<PaymentStatus> statusIn;

    @NotNull
    private TimeUnit timeUnit;
    private int interval;

    public void statusIn(PaymentStatus... statuses) {
        if (statuses.length > 0) {
            statusIn = new ArrayList<>(Arrays.asList(statuses));
        }
    }
}
