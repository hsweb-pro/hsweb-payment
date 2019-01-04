package org.hswebframework.payment.payment.service.impl;

import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.enums.TimeUnit;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.MerchantTradingMonitorRequest;
import org.hswebframework.payment.api.payment.monitor.IntervalMonitorResult;
import org.hswebframework.payment.api.payment.monitor.PaymentMonitor;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.payment.dao.PaymentOrderDao;
import org.hswebframework.payment.payment.entity.GroupByChannelResult;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.hswebframework.web.commons.entity.param.QueryParamEntity.newQuery;

@Component
public class RealTimePaymentMonitor implements PaymentMonitor {

    @Autowired
    private PaymentOrderDao orderDao;

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Override
    public long sumTradingAmount(MerchantTradingMonitorRequest request) {
        //按笔数统计
        if (request.getTimeUnit() == TimeUnit.SINGLE) {
            return newQuery()
                    .where()
                    .select("amount", "realAmount", "status")//只查询三个字段
                    .when(Objects.isNull(request.getStatus())&&Objects.isNull(request.getStatusIn()),//默认查询3种状态
                            query -> query.in(PaymentOrder::getStatus, PaymentStatus.success, PaymentStatus.paying, PaymentStatus.prepare))
                    .when(Objects.isNull(request.getTransType()),//默认只查询2种交易类型
                            query -> query.in(PaymentOrder::getTransType, TransType.GATEWAY, TransType.QUICK))
                    .in(MerchantTradingMonitorRequest::getStatus,request.getStatusIn())
                    .and(request::getTransType)
                    .and(request::getStatus)
                    .and(request::getMerchantId)
                    .and(request::getChannel)
                    .and(request::getChannelId)
                    .and("merchantId$agent$children", request.getAgentId()) //代理下所有的商户数据(含子级代理)
                    .doPaging(0, request.getInterval()) //分页查询指定的间隔
                    .orderByDesc(PaymentOrder::getCreateTime) //按创建事件排序
                    .execute(orderDao::query) //执行查询
                    .stream()
                    //如果订单为成功,则计算realAmount,否则计算amount
                    .mapToLong(order -> PaymentStatus.success == order.getStatus() ? order.getRealAmount() : order.getAmount())
                    .sum();
        }
        //获取时间间隔
        TimeUnit.TimeInterval timeInterval = request.getTimeUnit().getBeforeNowInterval(request.getInterval());
        return sumByInterval(timeInterval, request);
    }

    protected long doSumByInterval(TimeUnit.TimeInterval timeInterval, MerchantTradingMonitorRequest request) {
        //查询数据库
        return newQuery()
                .where()
                .and(request::getStatus)
                .in(MerchantTradingMonitorRequest::getStatus,request.getStatusIn())
                .and(request::getTransType)
                .when(request.getTransType() == null,
                        query -> query.in(PaymentOrder::getTransType, TransType.GATEWAY, TransType.QUICK))
                .and(request::getMerchantId)
                .and(request::getChannel)
                .and(request::getChannelId)
                .and("merchantId$agent$children", request.getAgentId())
                .between(PaymentOrder::getCompleteTime, timeInterval.getFrom(), timeInterval.getTo())
                .execute(orderDao::sumAmount);
    }

    protected List<GroupByChannelResult> doCountByIntervalGroupByChanel(TimeUnit.TimeInterval timeInterval,
                                                                        MerchantTradingMonitorRequest request) {
        //查询数据库
        return newQuery()
                .where()
                .and(request::getStatus)
                .in(MerchantTradingMonitorRequest::getStatus,request.getStatusIn())
                .and("merchantId$agent$children", request.getAgentId())
                .when(request.getTransType() == null,
                        query -> query.in(PaymentOrder::getTransType, TransType.GATEWAY, TransType.QUICK))
                .and(request::getTransType)
                .and(request::getMerchantId)
                .and(request::getChannel)
                .and(request::getChannelId)
                .between(PaymentOrder::getCompleteTime, timeInterval.getFrom(), timeInterval.getTo())
                .execute(orderDao::countGroupByChannel);
    }


    protected List<GroupByChannelResult> doSumByIntervalGroupByChanel(TimeUnit.TimeInterval timeInterval,
                                                                      MerchantTradingMonitorRequest request) {
        //查询数据库
        return newQuery()
                .where()
                .and(request::getStatus)
                .in(MerchantTradingMonitorRequest::getStatus,request.getStatusIn())
                .and("merchantId$agent$children", request.getAgentId())
                .and(request::getTransType)
                .when(request.getTransType() == null,
                        query -> query.in(PaymentOrder::getTransType, TransType.GATEWAY, TransType.QUICK))
                .and(request::getMerchantId)
                .and(request::getChannel)
                .and(request::getChannelId)
                .between(PaymentOrder::getCompleteTime, timeInterval.getFrom(), timeInterval.getTo())
                .execute(orderDao::sumAmountGroupByChannel);
    }

    protected long doCountInterval(TimeUnit.TimeInterval timeInterval, MerchantTradingMonitorRequest request) {
        //查询数据库
        return newQuery()
                .where()
                .and("merchantId$agent$children", request.getAgentId())
                .and(request::getStatus)
                .in(MerchantTradingMonitorRequest::getStatus,request.getStatusIn())
                .and(request::getTransType)
                .when(request.getTransType() == null,
                        query -> query.in(PaymentOrder::getTransType, TransType.GATEWAY, TransType.QUICK))
                .and(request::getMerchantId)
                .and(request::getChannel)
                .and(request::getChannelId)
                .between(PaymentOrder::getCompleteTime, timeInterval.getFrom(), timeInterval.getTo())
                .execute(orderDao::count);
    }

    protected <T> T cachedExecute(TimeUnit.TimeInterval interval,
                                  Supplier<T> executor,
                                  String cacheName, String cacheKey) {
        //没有查询今天的数据则使用缓存
        if (cacheManager != null && !interval.in(new Date())) {
            Cache cache = cacheManager.getCache(cacheName);
            String key = interval.getFrom().getTime() + "-" + interval.getTo().getTime() + "-" + cacheKey;
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper == null) {
                T sum = executor.get();
                cache.put(key, sum);
                return sum;
            } else {
                return (T) wrapper.get();
            }
        }
        return executor.get();
    }

    protected long sumByInterval(TimeUnit.TimeInterval timeInterval, MerchantTradingMonitorRequest request) {
        return cachedExecute(timeInterval,
                () -> doSumByInterval(timeInterval, request)
                , "payment:monitor:interval:sum"
                , String.valueOf(request.hashCode()));

    }

    protected long countByInterval(TimeUnit.TimeInterval timeInterval, MerchantTradingMonitorRequest request) {
        return cachedExecute(timeInterval,
                () -> doCountInterval(timeInterval, request)
                , "payment:monitor:interval:count"
                , String.valueOf(request.hashCode()));

    }

    @Override
    public List<IntervalMonitorResult> countIntervalTradingGroupByChannel(MerchantTradingMonitorRequest request, int numbers) {
        return createTimeInterval(request.getTimeUnit(), request.getInterval(), numbers)
                .parallelStream()
                .flatMap(interval -> cachedExecute(interval,
                        () -> doCountByIntervalGroupByChanel(interval, request),
                        "payment:monitor:interval:count-by-channel",
                        String.valueOf(request.hashCode()))
                        .stream()
                        .map(result -> IntervalMonitorResult.of(interval, result.getTotal(), result.getChannelName()))
                )
                .sorted(Comparator.comparing(result -> result.getTimeInterval().getFrom()))
                .collect(Collectors.toList());
    }

    @Override
    public List<IntervalMonitorResult> sumIntervalTradingGroupByChannel(MerchantTradingMonitorRequest request, int numbers) {
        return createTimeInterval(request.getTimeUnit(), request.getInterval(), numbers)
                .parallelStream()
                .flatMap(interval -> cachedExecute(interval,
                        () -> doSumByIntervalGroupByChanel(interval, request),
                        "payment:monitor:interval:sum-by-channel",
                        String.valueOf(request.hashCode()))
                        .stream()
                        .map(result -> IntervalMonitorResult.of(interval, result.getTotal(), result.getChannelName()))
                )
                .sorted(Comparator.comparing(result -> result.getTimeInterval().getFrom()))
                .collect(Collectors.toList());
    }

    @Override
    public List<IntervalMonitorResult> countIntervalTrading(MerchantTradingMonitorRequest request, int numbers) {
        return createTimeInterval(request.getTimeUnit(), request.getInterval(), numbers)
                .parallelStream()
                .map(interval -> IntervalMonitorResult.of(interval, countByInterval(interval, request)))
                .sorted(Comparator.comparing(result -> result.getTimeInterval().getFrom()))
                .collect(Collectors.toList());
    }

    protected List<TimeUnit.TimeInterval> createTimeInterval(TimeUnit timeUnit, int interval, int numbers) {
        //第一个时间
        TimeUnit.TimeInterval lastTimeInterval = timeUnit.getBeforeNowInterval(interval);
        List<TimeUnit.TimeInterval> intervals = new ArrayList<>(numbers);
        intervals.add(lastTimeInterval);
        Date lastDate = lastTimeInterval.getFrom();

        for (int i = 1; i < numbers; i++) {
            TimeUnit.TimeInterval nextInterval = timeUnit
                    .getBeforeInterval(new DateTime(lastDate).plusDays(-1).toDate(), interval);
            intervals.add(nextInterval);
            lastDate = nextInterval.getFrom();
        }
        return intervals;
    }

    @Override
    public List<IntervalMonitorResult> sumIntervalTradingAmount(MerchantTradingMonitorRequest request, int numbers) {

        return createTimeInterval(request.getTimeUnit(), request.getInterval(), numbers)
                .parallelStream()
                .map(interval -> IntervalMonitorResult.of(interval, sumByInterval(interval, request)))
                .sorted(Comparator.comparing(result -> result.getTimeInterval().getFrom()))
                .collect(Collectors.toList());
    }
}
