package org.hswebframework.payment.payment.channel.alipay.substitute;

import org.hswebframework.payment.api.payment.substitute.response.SubstituteResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class AlipaySubstituteResponse extends SubstituteResponse {
    private String batchNo;

    private String batchTransId;
}
