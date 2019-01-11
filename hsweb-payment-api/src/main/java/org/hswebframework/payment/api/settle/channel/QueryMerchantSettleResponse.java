package org.hswebframework.payment.api.settle.channel;

import org.hswebframework.payment.api.ApiResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class QueryMerchantSettleResponse extends ApiResponse {

    /**
     * 总入账资金
     */
    private long inAmount;

    /**
     * 总出账资金
     */
    private long outAmount;

    /**
     * @return 可用余额
     */
    public long getAmount() {
        return inAmount - outAmount;
    }

}
