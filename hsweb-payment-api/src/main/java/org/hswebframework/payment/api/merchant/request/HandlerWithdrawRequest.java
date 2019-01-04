package org.hswebframework.payment.api.merchant.request;

import org.hswebframework.payment.api.ApiRequest;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandlerWithdrawRequest extends ApiRequest {

    @NotBlank(message = "提现申请ID不能为空")
    private String withdrawId;

//    @NotBlank
//    private String paymentId;

    @NotBlank(message = "提现渠道不能为空")
    private String channelId;
}
