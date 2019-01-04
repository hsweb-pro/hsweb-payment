package org.hswebframework.payment.api.payment.monitor;

import org.hswebframework.payment.api.payment.MerchantTradingMonitorRequest;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface PaymentMonitor {

    //查询单个区间
    long sumTradingAmount(MerchantTradingMonitorRequest request);

    /**
     * 统计多个时间区间的交易额
     *
     * @param request 查询条件
     * @param numbers 区间数量
     * @return 统计结果
     */
    List<IntervalMonitorResult> sumIntervalTradingAmount(MerchantTradingMonitorRequest request, int numbers);


    /**
     * 统计多个时间区间的交易量
     *
     * @param request 查询条件
     * @param numbers 区间数量
     * @return 统计结果
     */
    List<IntervalMonitorResult> countIntervalTrading(MerchantTradingMonitorRequest request, int numbers);

    List<IntervalMonitorResult> countIntervalTradingGroupByChannel(MerchantTradingMonitorRequest request, int numbers);

    List<IntervalMonitorResult> sumIntervalTradingGroupByChannel(MerchantTradingMonitorRequest request, int numbers);

}
