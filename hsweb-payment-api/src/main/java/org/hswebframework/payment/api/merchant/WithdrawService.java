package org.hswebframework.payment.api.merchant;

import org.hswebframework.payment.api.merchant.request.*;
import org.hswebframework.payment.api.merchant.response.*;
import org.hswebframework.payment.api.merchant.request.*;
import org.hswebframework.payment.api.merchant.response.*;

/**
 * 提现接口:
 * <p>
 * 1. 发起提现申请 {@link this#applyWithdraw(ApplyWithdrawRequest)}<br>
 * 2. 确认提现申请 {@link this#handleWithdraw(HandlerWithdrawRequest)}<br>
 * 3. 完成提现 {@link this#completeWithdraw(CompleteWithdrawRequest)},
 * 或关闭提现 {@link this#closeWithdraw(CloseWithdrawRequest)}<br>
 *
 * @author Lind
 * @author zhouhao
 * @since 1.0
 */
public interface WithdrawService {

    /**
     * 商户提现申请
     *
     * @param request 提现请求
     * @return 申请结果
     */
    ApplyWithdrawResponse applyWithdraw(ApplyWithdrawRequest request);

    /**
     * 确认提现申请, 确认后将对商户资金进行下账(提现金额+手续费).此时不会进行手续费上账
     *
     * @param request 确认请求
     * @return 确认结果
     */
    HandlerWithdrawResponse handleWithdraw(HandlerWithdrawRequest request);


    /**
     * 完成提现申请, 完成后,将会手续费入账到平台和代理
     *
     * @param request 完成提现请求
     * @return 完成提现结果
     */
    CompleteWithdrawResponse completeWithdraw(CompleteWithdrawRequest request);


    /**
     * 关闭提现申请,关闭后,将对商户对提现申请进行退款
     *
     * @param request 关闭提现请求
     * @return 关闭提现结果
     */
    CloseWithdrawResponse closeWithdraw(CloseWithdrawRequest request);


    /**
     * 查询提现申请
     *
     * @param request 查询提现申请
     * @return 查询结果
     */
    QueryWithdrawResponse queryWithdraw(QueryWithdrawRequest request);
}
