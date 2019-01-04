package org.hswebframework.payment.api.merchant.request;

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
public class CompleteWithdrawRequest {

    @NotBlank(message = "提现申请ID不能为空")
    private String withdrawId;

    private String completeProve;
}
