package org.hswebframework.payment.api.payment.substitute;

import org.hswebframework.payment.api.events.BusinessEvent;
import lombok.*;

/**
 * 代付完成事件
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubstituteDetailCompleteEvent implements BusinessEvent {

    private String paymentId;

    private String detailId;

    private long amount;

    private boolean success;

    private String memo;
}
