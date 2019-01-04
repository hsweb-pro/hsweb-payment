package org.hswebframework.payment.payment.controller;

import org.hswebframework.payment.api.enums.TimeUnit;
import org.hswebframework.payment.api.payment.MerchantTradingMonitorRequest;
import org.hswebframework.payment.api.payment.monitor.IntervalMonitorResult;
import org.hswebframework.payment.api.payment.monitor.PaymentMonitor;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/payment")
@Authorize(permission = "payment")
public class PaymentMonitorController {

    @Autowired
    private PaymentMonitor paymentMonitor;

    @GetMapping("/monitor/trading/sum/{timeUnit}/{interval:\\d+}")
    public ResponseMessage<Long> sumTradingAmount(
            @PathVariable TimeUnit timeUnit,
            @PathVariable int interval,
            MerchantTradingMonitorRequest request) {
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        return ResponseMessage.ok(paymentMonitor.sumTradingAmount(request));
    }

    /**
     * 近七天每天交易额数据: /monitor/trading/sum/DAY/1/7
     * <p>
     * 统计5次每2个月为一个间隔的数据: /monitor/trading/sum/MONTH/2/5
     */
    @GetMapping("/monitor/trading/sum/{timeUnit}/{interval:\\d+}/{numbers:\\d+}")
    public ResponseMessage<List<IntervalMonitorResult>> sumTradingAmount(
            @PathVariable TimeUnit timeUnit,
            @PathVariable int interval,
            @PathVariable int numbers,
            MerchantTradingMonitorRequest request) {
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        return ResponseMessage.ok(paymentMonitor.sumIntervalTradingAmount(request, numbers));
    }

    @GetMapping("/monitor/trading/count/{timeUnit}/{interval:\\d+}/{numbers:\\d+}")
    public ResponseMessage<List<IntervalMonitorResult>> countTrading(
            @PathVariable TimeUnit timeUnit,
            @PathVariable int interval,
            @PathVariable int numbers,
            MerchantTradingMonitorRequest request) {
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        return ResponseMessage.ok(paymentMonitor.countIntervalTrading(request, numbers));
    }

    @GetMapping("/monitor/trading/count-by-channel/{timeUnit}/{interval:\\d+}/{numbers:\\d+}")
    public ResponseMessage<List<IntervalMonitorResult>> countTradingGroupByChannel(
            @PathVariable TimeUnit timeUnit,
            @PathVariable int interval,
            @PathVariable int numbers,
            MerchantTradingMonitorRequest request) {
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        return ResponseMessage.ok(paymentMonitor.countIntervalTradingGroupByChannel(request, numbers));
    }

    @GetMapping("/monitor/trading/sum-by-channel/{timeUnit}/{interval:\\d+}/{numbers:\\d+}")
    public ResponseMessage<List<IntervalMonitorResult>> sumTradingGroupByChannel(
            @PathVariable TimeUnit timeUnit,
            @PathVariable int interval,
            @PathVariable int numbers,
            MerchantTradingMonitorRequest request) {
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        return ResponseMessage.ok(paymentMonitor.sumIntervalTradingGroupByChannel(request, numbers));
    }
}
