package org.hswebframework.payment.api.settle.channel;


public interface ChannelSettleService {

    /**
     * 渠道结算上账
     *
     * @param request 上账请求
     * @return 请求结果
     */
    ChannelDepositResponse deposit(ChannelDepositRequest request);

    /**
     * 渠道结算下账
     *
     * @param request 下账请求
     * @return 请求结果
     */
    ChannelWithdrawResponse withdraw(ChannelWithdrawRequest request);

    ChannelSettleInfo getInfoByAccountNo(String accountNo);

    /**
     * 查询商户的渠道结算信息
     *
     * @param request 查询请求
     * @return 查询结果
     */
    QueryMerchantSettleResponse queryMerchantSettle(QueryMerchantSettleRequest request);
}
