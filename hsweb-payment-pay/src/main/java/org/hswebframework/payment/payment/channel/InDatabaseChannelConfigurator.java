package org.hswebframework.payment.payment.channel;

import org.hswebframework.payment.api.enums.*;
import org.hswebframework.payment.api.merchant.config.MerchantChannelConfig;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.payment.*;
import org.hswebframework.payment.api.payment.monitor.PaymentMonitor;
import org.hswebframework.payment.api.selector.ConfigSelectorRule;
import org.hswebframework.payment.api.selector.SelectorRuleConfig;
import org.hswebframework.payment.api.utils.Money;
import org.hswebframework.payment.payment.entity.ChannelConfigEntity;
import org.hswebframework.payment.api.payment.events.PaymentLimitWarnEvent;
import org.hswebframework.payment.api.payment.events.PaymentOutOfLimitEvent;
import org.hswebframework.payment.payment.service.LocalChannelConfigService;
import org.hswebframework.payment.payment.service.impl.LocalMerchantBindPaymentChannelService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.concurrent.GuavaRateLimiterManager;
import org.hswebframework.web.concurrent.RateLimiterManager;
import org.hswebframework.web.concurrent.counter.Counter;
import org.hswebframework.web.concurrent.counter.CounterManager;
import org.hswebframework.web.concurrent.counter.SimpleCounterManager;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j(topic = "system.payment.channel.configurator")
public class InDatabaseChannelConfigurator<T extends ChannelConfig> implements PaymentChannelConfigurator<T> {

    private LocalChannelConfigService channelConfigService;

    private MerchantConfigManager configManager;

    private PaymentMonitor paymentMonitor;

    private LocalMerchantBindPaymentChannelService paymentChannelService;

    @Getter
    @Setter
    private String channel;

    @Getter
    @Setter
    private String channelProvider;

    @Getter
    @Setter
    private TransType transType;

    @Getter
    @Setter
    private Class<T> configType;

    @Getter
    @Setter
    private ApplicationContext context;

    private RateLimiterManager rateLimiterManager;

    private ApplicationEventPublisher eventPublisher;

    private CounterManager counterManager;

    public CounterManager getCounterManager() {
        if (counterManager == null) {
            synchronized (this) {
                try {
                    counterManager = context.getBean(CounterManager.class);
                } catch (NoSuchBeanDefinitionException e) {
                    counterManager = new SimpleCounterManager();
                }
            }
        }
        return counterManager;
    }

    private static Map<String, Counter> counterMap = new ConcurrentHashMap<>();

    static {
        new Thread(() -> {
            while (true) {
                try {
                    //每秒清空所有计数器
                    Thread.sleep(1000);
                    for (Counter counter : counterMap.values()) {
                        counter.set(0L);
                    }
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    break;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }).start();
    }

    public Counter getCounter(String key) {
        return counterMap.computeIfAbsent(key, k -> getCounterManager().getCounter("payment-counter:" + key));
    }

    public RateLimiterManager getRateLimiterManager() {
        if (rateLimiterManager == null) {
            synchronized (this) {
                try {
                    rateLimiterManager = context.getBean(RateLimiterManager.class);
                } catch (NoSuchBeanDefinitionException e) {
                    rateLimiterManager = new GuavaRateLimiterManager();
                }
            }
        }
        return rateLimiterManager;
    }

    public ApplicationEventPublisher getEventPublisher() {
        if (eventPublisher == null) {
            synchronized (this) {
                eventPublisher = context;
            }
        }
        return eventPublisher;
    }

    protected LocalChannelConfigService getChannelConfigService() {
        if (channelConfigService == null) {
            synchronized (this) {
                channelConfigService = context.getBean(LocalChannelConfigService.class);
            }
        }
        return channelConfigService;
    }

    protected MerchantConfigManager getConfigManager() {
        if (configManager == null) {
            synchronized (this) {
                configManager = context.getBean(MerchantConfigManager.class);
            }
        }
        return configManager;
    }

    private PaymentMonitor getPaymentMonitor() {
        if (paymentMonitor == null) {
            synchronized (this) {
                paymentMonitor = context.getBean(PaymentMonitor.class);
            }
        }
        return paymentMonitor;
    }

    public LocalMerchantBindPaymentChannelService getPaymentChannelService() {
        if (paymentChannelService == null) {
            synchronized (this) {
                paymentChannelService = context.getBean(LocalMerchantBindPaymentChannelService.class);
            }
        }
        return paymentChannelService;
    }


    protected List<T> getAllConfig() {
        //判断是否独占
        return getChannelConfigService()
                .queryByTransTypeAndChannel(this.transType, channel, channelProvider)
                .stream()
                .map(entity -> entity.toChannelConfig(configType))
                .collect(Collectors.toList());
    }

    protected boolean isOutOfLimit(String merchantId,
                                   LimitScope limitScope,
                                   List<TradingLimit> limits,
                                   BiConsumer<TradingLimit, MerchantTradingMonitorRequest> consumer,
                                   long amount) {
        return limits.stream()
                .anyMatch(limit -> {
                    MerchantTradingMonitorRequest request = MerchantTradingMonitorRequest.builder().build();
                    consumer.accept(limit, request);
                    request.setTimeUnit(limit.getTimeUnit());
                    request.setInterval(limit.getInterval());
                    if (limit.getTimeUnit() == TimeUnit.SINGLE) {
                        //单笔限额
                        if (limit.getInterval() == 1) {
                            boolean outOfLimit = limit.getLimit() <= amount;
                            if (outOfLimit) {
                                log.warn("交易超过限额:{},本次交易额:{}", limit, Money.cent(amount));
                            }
                            //单比限额不做预警
                            return outOfLimit;
                        }
                        //多笔限额,少查询一笔(最后还要加上本次交易)
                        request.setInterval(limit.getInterval() - 1);
                    }
                    long tradAmount = getPaymentMonitor().sumTradingAmount(request);
                    log.info("本次交易额:{},交易总额:{},限额:{}", Money.cent(amount), Money.cent(tradAmount), Money.cent(limit.getLimit()));
                    //当前交易额+历史交易额 >= 限额
                    boolean outOfLimit = amount + tradAmount >= limit.getLimit();
                    boolean doWarnLimit = limit.getWarnLimit() > 0 && amount + tradAmount >= limit.getWarnLimit();

                    if (outOfLimit) {
                        log.warn("交易超过限额:{},历史交易额:{},本次交易额:{}"
                                , limit, Money.cent(tradAmount), Money.cent(amount));
                        getEventPublisher().publishEvent(PaymentOutOfLimitEvent
                                .builder()
                                .merchantId(merchantId)
                                .limitScope(limitScope)
                                .channel(getChannel())
                                .transType(getTransType())
                                .limit(limit)
                                .amount(amount)
                                .build());
                    } else if (doWarnLimit) {
                        log.warn("交易已超过限额警告阈值:{},历史交易额:{},本次交易额:{}"
                                , Money.cent(limit.getWarnLimit())
                                , Money.cent(tradAmount)
                                , Money.cent(amount));
                        //警告事件
                        getEventPublisher().publishEvent(PaymentLimitWarnEvent
                                .builder()
                                .merchantId(merchantId)
                                .limitScope(limitScope)
                                .channel(getChannel())
                                .transType(getTransType())
                                .limit(limit)
                                .amount(amount)
                                .build());
                    }
                    return outOfLimit;
                });
    }

    protected BiPredicate<T, Long> createLimitFilter(String merchantId,
                                                     MerchantChannelConfig merchantChannelConfig) {
        return (cfg, amount) -> {
            //渠道限额
            List<TradingLimit> limits = cfg.getTradingLimits() == null ? null : new ArrayList<>(cfg.getTradingLimits());
            boolean outOfLimit = false;
            if (!CollectionUtils.isEmpty(limits)) {
                log.info("执行渠道[{}-{}]限额判断,本次交易金额:{},限额:{}",
                        merchantChannelConfig.getChannelName(),
                        cfg.getName(),
                        Money.cent(amount), limits);

                outOfLimit = isOutOfLimit(merchantId, LimitScope.CHANNEL, limits, (tradingLimit, request) -> {
                    request.setTransType(getTransType());
                    request.setChannelId(cfg.getId());
                }, amount);
            }

            //对商户的单个渠道限额
            List<TradingLimit> exactTradingLimits = merchantChannelConfig.getExactTradingLimit(cfg.getId());
            if (!outOfLimit && !CollectionUtils.isEmpty(exactTradingLimits)) {
                log.info("执行商户渠道[{}-{}]限额判断,本次交易金额:{},限额:{}",
                        merchantChannelConfig.getChannelName(),
                        cfg.getName(), Money.cent(amount), exactTradingLimits);

                outOfLimit = isOutOfLimit(merchantId, LimitScope.MERCHANT_CHANNEL_SINGLE, exactTradingLimits, (tradingLimit, request) -> {
                    request.setTransType(getTransType());
                    request.setMerchantId(merchantId);
                    request.setChannelId(cfg.getId());
                }, amount);
            }

            return !outOfLimit;
        };
    }

    protected ConfigSelectorRule<? extends SelectorRuleConfig, T> createRule(String merchantId, MerchantChannelConfig config) {
        SelectorRule selectorRule = Optional.ofNullable(config.getSelectorRule()).orElse(SelectorRule.RANDOM);
        ConfigSelectorRule<? extends SelectorRuleConfig, T> rule = selectorRule.createRule(config.createRuleConfig(), () -> {
            List<T> configs = this.getAllConfig();
            if (configs.isEmpty()) {
                return configs;
            }
            //获取商户不能使用的配置(其他商户独占的配置)
            List<String> notMe = getPaymentChannelService()
                    .selectNotInMerchantBind(merchantId, configs
                            .stream()
                            .map(ChannelConfig::getId)
                            .collect(Collectors.toList()));

            return configs.stream()
                    //过滤掉其他商户独占的配置
                    .filter(conf -> !notMe.contains(conf.getId()))
                    .filter(conf -> {
                        if (conf.getMaximumTradingPerSecond() <= 0D) {
                            return true;
                        }
                        // 选择没有超过限流对渠道
                        boolean outOfLimit = getCounter(conf.getId()).get() >= conf.getMaximumTradingPerSecond();
                        if (outOfLimit) {
                            log.warn("渠道[{}]超过每秒最大交易量[{}/s]", conf.getName(), conf.getMaximumTradingPerSecond());
                        }
                        return !outOfLimit;
                    })
                    .collect(Collectors.toList());
        });
        //选择了一个渠道
        rule.onSelected(conf -> {
            if (conf.getMaximumTradingPerSecond() <= 0D) {
                return;
            }
            //对选择对渠道计数+1
            getCounter(conf.getId()).increment();
            //限流处理.最大等待5秒
            boolean success = getRateLimiterManager()
                    .getRateLimiter("channel-config:" + conf.getId(), conf.getMaximumTradingPerSecond(), java.util.concurrent.TimeUnit.SECONDS)
                    .tryAcquire(5, java.util.concurrent.TimeUnit.SECONDS);
            if (!success) {
                throw ErrorCode.SERVICE_BUSY.createException();
            }

        });
        rule.setLimitFilter(createLimitFilter(merchantId, config));
        return rule;
    }

    //过滤开通了的渠道
    private Predicate<MerchantChannelConfig> configMatcher = config ->
            (StringUtils.isEmpty(config.getChannelProvider())
                    || getChannelProvider().equals(config.getChannelProvider()))
                    && getChannel().equals(config.getChannel())
                    && transType == config.getTransType()
                    && config.isEnabled();

    @Override
    public T getPaymentConfigByMerchantId(String merchantId, TransType transType, long amount) {
        return getConfigManager()
                .<MerchantChannelConfig>getConfigList(merchantId, MerchantConfigKey.SUPPORTED_CHANNEL)
                .map(list ->
                        list.stream()
                                .filter(configMatcher)
                                .findFirst()
                                .map(cfg -> {
                                    //商户的渠道总限额
                                    List<TradingLimit> merchantLimits = cfg.getTradingLimits();
                                    if (!CollectionUtils.isEmpty(merchantLimits)) {
                                        log.info("执行商户渠道[{}]总限额判断,本次交易金额:{},限额:{}",
                                                cfg.getChannelName(), Money.cent(amount), merchantLimits);
                                        boolean outOfLimit =
                                                isOutOfLimit(merchantId, LimitScope.MERCHANT_CHANNEL, merchantLimits, (tradingLimit, request) -> {
                                                    request.setTransType(getTransType());
                                                    request.setMerchantId(merchantId);
                                                    request.setChannel(getChannel());
                                                    request.setChannelProvider(getChannelProvider());
                                                }, amount);
                                        if (outOfLimit) {
                                            throw ErrorCode.CHANEL_OUT_OF_LIMIT.createException();
                                        }
                                    }
                                    return cfg;
                                })
                                .map(cfg -> this.createRule(merchantId, cfg))
                                .map(rule -> rule.select(amount))
                                .orElse(null)
                ).orElse(null);

    }

    @Override
    public T getPaymentConfigById(String channelId) {
        ChannelConfigEntity entity = getChannelConfigService().selectByPk(channelId);
        if (entity == null) {
            return null;
        }
        return entity.toChannelConfig(configType);
    }
}