package org.hswebframework.payment.api.account.response;

import lombok.EqualsAndHashCode;
import org.hswebframework.payment.api.ApiResponse;
import org.hswebframework.payment.api.account.AccountTransLog;
import lombok.Data;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccountTransLogResponse extends ApiResponse {

    private List<AccountTransLog> transLogList;
}
