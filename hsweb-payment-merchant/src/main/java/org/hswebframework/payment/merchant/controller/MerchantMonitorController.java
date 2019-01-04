package org.hswebframework.payment.merchant.controller;

import org.hswebframework.payment.api.enums.TimeUnit;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.payment.MerchantTradingMonitorRequest;
import org.hswebframework.payment.api.payment.monitor.IntervalMonitorResult;
import org.hswebframework.payment.api.payment.monitor.PaymentMonitor;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Lind
 * @since 1.0
 */
@RestController
@RequestMapping("/merchant/monitor")
public class MerchantMonitorController {

    @Autowired
    private PaymentMonitor paymentMonitor;

    @GetMapping("sum/trading/{timeUnit}/{interval:\\d+}")
    public ResponseMessage<Long> sumTradingOrder(
            @PathVariable TimeUnit timeUnit,
            @PathVariable int interval,
            Merchant merchant){
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();

        request.setMerchantId(merchant.getId());
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        return ResponseMessage.ok(paymentMonitor.sumTradingAmount(request));
    }


    /**
     * 近七天每天交易额数据: /monitor/trading/sum/DAY/1/7
     * <p>
     * 统计5次每2个月为一个间隔的数据: /monitor/trading/sum/MONTH/2/5
     */
    @GetMapping("/sum/trading/{timeUnit}/{interval:\\d+}/{numbers:\\d+}")
    public ResponseMessage<List<IntervalMonitorResult>> sumTradingAmount(
            @PathVariable TimeUnit timeUnit,
            @PathVariable int interval,
            @PathVariable int numbers,
            Merchant merchant) {
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setMerchantId(merchant.getId());
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        return ResponseMessage.ok(paymentMonitor.sumIntervalTradingAmount(request, numbers));
    }



    @GetMapping("/trading/count-by-channel/{timeUnit}/{interval:\\d+}/{numbers:\\d+}")
    public ResponseMessage<List<IntervalMonitorResult>> countTradingGroupByChannel(
            @PathVariable TimeUnit timeUnit,
            @PathVariable int interval,
            @PathVariable int numbers,
            Merchant merchant) {
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setMerchantId(merchant.getId());
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        return ResponseMessage.ok(paymentMonitor.countIntervalTradingGroupByChannel(request, numbers));
    }

    @GetMapping("/trading/sum-by-channel/{timeUnit}/{interval:\\d+}/{numbers:\\d+}")
    public ResponseMessage<List<IntervalMonitorResult>> sumTradingGroupByChannel(
            @PathVariable TimeUnit timeUnit,
            @PathVariable int interval,
            @PathVariable int numbers,
            Merchant merchant) {
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setMerchantId(merchant.getId());
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        return ResponseMessage.ok(paymentMonitor.sumIntervalTradingGroupByChannel(request, numbers));
    }

}
