package org.hswebframework.payment.merchant.service.impl;

import org.hswebframework.payment.api.charge.ChargeService;
import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.enums.TimeUnit;
import org.hswebframework.payment.api.enums.TransRateType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.merchant.AgentMerchant;
import org.hswebframework.payment.api.merchant.AgentMerchantService;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.config.MerchantRateConfig;
import org.hswebframework.payment.api.payment.MerchantTradingMonitorRequest;
import org.hswebframework.payment.api.payment.monitor.PaymentMonitor;
import io.vavr.Function4;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hswebframework.payment.api.utils.MerchantRateChargeUtils.findConfig;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
@Slf4j
public class DefaultChargeService implements ChargeService {

    @Autowired
    private MerchantConfigManager configManager;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private AgentMerchantService agentMerchantService;

    @Autowired
    private PaymentMonitor paymentMonitor;

    /**
     * 计算代理费率
     *
     * @param merchantId
     * @param transType
     * @param channel
     * @param amount
     * @param agentChargeConsumer
     */
    @Override
    public void calculate(String merchantId,
                          TransType transType,
                          String channel,
                          long amount,
                          boolean calculateAgent,
                          Consumer<TransRateType.TransCharge> chargeConsumer, //平台收费回调
                          Consumer<TransRateType.TransCharge> merchantChargeConsumer,//商户总计收费回调
                          //多级代理收费回调
                          Function4<AgentMerchant, TransRateType.TransCharge, Long, TransRateType.TransCharge, Void> agentChargeConsumer) {

        final AtomicReference<TransRateType.TransCharge> lastCharge = new AtomicReference<>(TransRateType.TransCharge.none);
        //已经计算过的代理,用于处理循环关联的代理
        Set<String> calculated = new HashSet<>();

        //获取参与计算费率的金额
        Function<MerchantRateConfig, Supplier<Long>> amountGetter = (config) -> {
            TimeUnit timeUnit = config.getChargeTimeUnit();
            //根据单笔交易计算
            if (timeUnit == null || timeUnit == TimeUnit.SINGLE) {
                return () -> amount;
            }
            return () -> {
                //统计交易额
                MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
                request.setTransType(transType);
                request.setMerchantId(merchantId);
                request.setChannel(channel);
                request.setTimeUnit(timeUnit);
                request.setInterval(config.getChargeInterval());
                request.statusIn(PaymentStatus.success, PaymentStatus.paying, PaymentStatus.prepare);
                return amount + paymentMonitor.sumTradingAmount(request);
            };
        };
        //商户的费率
        findConfig(configManager, merchantId, transType, channel)
                .ifPresent(config -> config.getRateType()
                        .calculate(amount, amountGetter.apply(config), config.getRate())
                        .ifPresent(charge -> {
                            lastCharge.set(charge);
                            merchantChargeConsumer.accept(charge);
                        }));
        if (calculateAgent) {
            String agentId = null;
            //是商户
            Merchant merchant = merchantService.getMerchantById(merchantId);
            if (merchant != null) {
                agentId = merchant.getAgentId();
            } else {
                //是代理
                AgentMerchant agentMerchant = agentMerchantService.getAgentById(merchantId);
                if (agentMerchant != null) {
                    agentId = agentMerchant.getParentId();
                }
            }
            AgentMerchant agentMerchant = agentMerchantService.getAgentById(agentId);
            //循环计算代理,从下到上,层层剥削
            for (; agentMerchant != null; ) {
                AgentMerchant currentAgent = agentMerchant;
                if (StringUtils.hasText(agentMerchant.getParentId())
                        && calculated.contains(agentMerchant.getParentId())) {
                    log.error("可能存在循环代理:all:{} => id:{},parentId:{}", calculated, agentMerchant.getId(), agentMerchant.getParentId());
                    break;
                }
                //计算代理费率
                findConfig(configManager, currentAgent.getId(), transType, channel)
                        .ifPresent(config -> config.getRateType()
                                .calculate(amount, amountGetter.apply(config), config.getRate())
                                .ifPresent(charge -> {
                                    //代理实际收费=商户(下级代理)费率-代理费率
                                    long realCharge = lastCharge.get().getCharge() - charge.getCharge();
                                    agentChargeConsumer.apply(currentAgent, charge, realCharge, lastCharge.get());
                                    lastCharge.set(charge);
                                    calculated.add(currentAgent.getId());
                                }));

                //计算上一级
                agentMerchant = agentMerchantService.getAgentById(agentMerchant.getParentId());
            }
        }
        chargeConsumer.accept(lastCharge.get());
    }

}
