package org.hswebframework.payment.payment.channel;

import org.hswebframework.payment.api.enums.MerchantConfigKey;
import org.hswebframework.payment.api.enums.SelectorRule;
import org.hswebframework.payment.api.enums.TimeUnit;
import org.hswebframework.payment.api.enums.TransType;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
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

    private ApplicationEventPublisher eventPublisher;


    public ApplicationEventPublisher getEventPublisher() {
        if (eventPublisher == null) {
            synchronized (this) {
                eventPublisher = context.getBean(ApplicationEventPublisher.class);
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
        return limits.stream().anyMatch(limit -> {
            MerchantTradingMonitorRequest request = MerchantTradingMonitorRequest.builder().build();
            consumer.accept(limit, request);
            request.setTimeUnit(limit.getTimeUnit());
            request.setInterval(limit.getInterval());
            if (limit.getTimeUnit() == TimeUnit.SINGLE) {
                //单笔限额
                if (limit.getInterval() == 1) {
                    //单比限额不做预警
                    return limit.getLimit() <= amount;
                }
                //多笔限额,少查询一笔(最后还要加上本次交易)
                request.setInterval(limit.getInterval() - 1);
            }
            long tradAmount = getPaymentMonitor().sumTradingAmount(request);

            //当前交易额+历史交易额 >= 限额
            boolean outOfLimit = amount + tradAmount >= limit.getLimit();

            boolean doWarnLimit = limit.getWarnLimit() > 0 && amount + tradAmount >= limit.getWarnLimit();
            if (outOfLimit) {
                log.warn("交易超过限额:{},历史交易额:{},本次交易额:{}"
                        , limit, Money.cent(tradAmount), Money.cent(amount));
                eventPublisher.publishEvent(PaymentOutOfLimitEvent
                        .builder()
                        .merchantId(merchantId)
                        .limitScope(limitScope)
                        .channel(request.getChannel())
                        .transType(request.getTransType())
                        .limit(limit)
                        .amount(amount)
                        .build());
            } else if (doWarnLimit) {
                log.warn("交易已超过限额警告阈值:{},历史交易额:{},本次交易额:{}"
                        , Money.cent(limit.getWarnLimit()),
                        Money.cent(tradAmount));
                //警告事件
                eventPublisher.publishEvent(PaymentLimitWarnEvent
                        .builder()
                        .merchantId(merchantId)
                        .limitScope(limitScope)
                        .channel(request.getChannel())
                        .transType(request.getTransType())
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
            List<TradingLimit> limits = cfg.getTradingLimits();
            boolean outOfLimit = false;
            if (!CollectionUtils.isEmpty(limits)) {
                log.info("执行渠道[{}]总限额判断,本次交易金额:{},限额:{}",
                        merchantChannelConfig.getChannelName(),
                        Money.cent(amount), limits);
                outOfLimit = isOutOfLimit(merchantId, LimitScope.CHANNEL, limits, (tradingLimit, request) -> {
                    request.setTransType(getTransType());
                    request.setChannel(getChannel());
                }, amount);
            }

            //商户的渠道总限额
            List<TradingLimit> merchantLimits = merchantChannelConfig.getTradingLimits();
            if (!outOfLimit && !CollectionUtils.isEmpty(merchantLimits)) {
                log.info("执行商户渠道[{}]总限额判断,本次交易金额:{},限额:{}",
                        merchantChannelConfig.getChannelName(), Money.cent(amount), merchantLimits);
                outOfLimit = isOutOfLimit(merchantId, LimitScope.MERCHANT_CHANNEL, merchantLimits, (tradingLimit, request) -> {
                    request.setTransType(getTransType());
                    request.setMerchantId(merchantId);
                    request.setChannel(getChannel());
                }, amount);
            }

            //对商户的单个渠道限额
            List<TradingLimit> exactTradingLimits = merchantChannelConfig.getExactTradingLimit(cfg.getId());
            if (!outOfLimit && !CollectionUtils.isEmpty(exactTradingLimits)) {
                log.info("执行商户渠道[{}:{}]限额判断,本次交易金额:{},限额:{}",
                        merchantChannelConfig.getChannelName(),
                        cfg.getName(), Money.cent(amount), exactTradingLimits);

                outOfLimit = isOutOfLimit(merchantId, LimitScope.MERCHANT_CHANNEL_SINGLE, exactTradingLimits, (tradingLimit, request) -> {
                    request.setTransType(getTransType());
                    request.setMerchantId(merchantId);
                    request.setChannel(getChannel());
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
            //其他商户没有独占则直接返回
            if (notMe.isEmpty()) {
                return configs;
            }
            //过滤掉其他商户独占的配置
            return configs.stream()
                    .filter(conf -> !notMe.contains(conf.getId()))
                    .collect(Collectors.toList());
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
