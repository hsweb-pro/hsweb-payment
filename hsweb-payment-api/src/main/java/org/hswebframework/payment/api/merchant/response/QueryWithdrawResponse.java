package org.hswebframework.payment.api.merchant.response;

import org.hswebframework.payment.api.ApiResponse;
import org.hswebframework.payment.api.merchant.MerchantWithdrawLog;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
public class QueryWithdrawResponse extends ApiResponse {

    private List<MerchantWithdrawLog> withdrawLogList;

    private Integer total;
}
