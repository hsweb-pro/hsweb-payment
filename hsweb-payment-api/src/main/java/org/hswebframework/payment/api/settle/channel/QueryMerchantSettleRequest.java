package org.hswebframework.payment.api.settle.channel;

import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.enums.TransType;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryMerchantSettleRequest extends ApiRequest {

    @NotBlank(message = "商户ID不能为空")
    private String merchantId;

    private TransType transType;

    private String channelId;
}
