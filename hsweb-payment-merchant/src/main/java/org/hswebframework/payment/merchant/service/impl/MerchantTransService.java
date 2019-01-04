package org.hswebframework.payment.merchant.service.impl;

import org.hswebframework.payment.api.account.Account;
import org.hswebframework.payment.api.account.AccountService;
import org.hswebframework.payment.api.account.AccountTransService;
import org.hswebframework.payment.api.account.reqeust.AccountQueryRequest;
import org.hswebframework.payment.api.account.reqeust.AccountTransRequest;
import org.hswebframework.payment.api.account.response.AccountQueryResponse;
import org.hswebframework.payment.api.account.response.AccountTransResponse;
import org.hswebframework.payment.api.charge.ChargeService;
import org.hswebframework.payment.api.enums.CurrencyEnum;
import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.enums.TransRateType;
import org.hswebframework.payment.api.enums.TransRateType.TransCharge;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.merchant.AgentMerchant;
import org.hswebframework.payment.api.merchant.AgentMerchantService;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.payment.api.payment.ChannelConfig;
import org.hswebframework.payment.api.payment.ChannelConfigManager;
import org.hswebframework.payment.api.payment.events.GatewayPaymentCompleteEvent;
import org.hswebframework.payment.api.payment.events.PaymentCompleteEvent;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.merchant.dao.MerchantChargeDao;
import org.hswebframework.payment.merchant.entity.MerchantChargeEntity;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 商户交易服务
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Component
@Slf4j(topic = "system.merchant.trans")
public class MerchantTransService {

    @Autowired
    private MerchantChargeDao chargeDao;

    @Autowired
    private ChannelConfigManager channelConfigManager;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private AccountTransService accountTransService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ChargeService chargeService;

    private void handlePaymentComplete(PaymentOrder order) {
        try {
            //商户
            Merchant merchant = merchantService.getMerchantById(order.getMerchantId());

            //订单金额
            long transAmount = order.getRealAmount();
            if (transAmount == 0) {
                log.error("订单[" + order.getId() + "]实收金额为0!");
                return;
//                throw ErrorCode.BUSINESS_FAILED.createException("订单[" + order.getId() + "]实收金额为0!");
            }

            MerchantChargeEntity chargeEntity = new MerchantChargeEntity();
            chargeEntity.setId(IDGenerator.SNOW_FLAKE_STRING.generate());
            chargeEntity.setAmount(transAmount);
            chargeEntity.setMerchantId(order.getMerchantId());
            chargeEntity.setTransType(order.getTransType());
            chargeEntity.setPaymentId(order.getId());
            chargeEntity.setPayTime(order.getUpdateTime());
            chargeEntity.setSettle(true); // 自动结算
            chargeEntity.setSettleTime(new Date());
            chargeEntity.setChannelCharge(0L);
            chargeEntity.setCharge(0L);
            chargeEntity.setAgentCharge(0L);
            chargeEntity.setChargeCalculated(false);
            List<Runnable> agentChargeJobs = new ArrayList<>();

            //计算收费
            chargeService.calculate(merchant.getId(),
                    order.getTransType(),
                    order.getChannel(),
                    transAmount,
                    true,
                    chargeEntity::chargeComplete,
                    chargeEntity::agentChargeComplete,
                    (agent, charge, real, parent) -> {
                        agentChargeJobs.add(() -> {
                            String remark=order.getMerchantName() + ":" + order.getChannelName()
                                    + ",代理分润;总交易额:"
                                    + TransCharge.format(transAmount)
                                    + "元;商户费率:" + parent.toString()
                                    + ";平台费率:" + charge.toString()
                                    + ";实际金额:" + TransCharge.format(real) + "元";

                            log.info("订单[{}]计算代理收费:{}",order.getId(),remark);
                            //代理商上账
                            accountDeposit(order.getId(), agent.getAccountNo(),
                                    agent.getId(),
                                    real,
                                    TransType.AGENT_CHARGE,
                                    order.getCurrency(),remark);
                        });
                        return null;
                    });

            //商户资金账户
            Account account = queryAccountByMerchant(merchant.getId());
            //入账


            //平台收费
            Long charge = chargeEntity.getCharge();
            //代理收费
            Long agentCharge = chargeEntity.getAgentCharge();

            if (transAmount > 0) {
                //给交易商户上账
                accountDeposit(order.getId(),
                        account.getAccountNo(),
                        merchant.getId(),
                        transAmount,
                        order.getTransType(),
                        order.getCurrency(),
                        order.getChannelName() + ":" + order.getProductName());
            }

            //平台资金账户上帐
            Runnable masterDeposit = () -> {
                //平台实际收入=平台收费-渠道收费
                AtomicLong realCharge = new AtomicLong(charge);
                AtomicReference<String> comment = new AtomicReference<>(order.getChannelName()
                        + "服务费:" + chargeEntity.getChargeMemo());
                //渠道收费计算
                channelConfigManager
                        .getChannelConfigById(order.getChannelId(), ChannelConfig.class)
                        .ifPresent(channelConfig -> {
                            //渠道收费
                            long channelCharge = channelConfig.calculateRate(transAmount);
                            chargeEntity.setChannelCharge(channelCharge);
                            //平台真实收费=收费-渠道收费
                            realCharge.set(charge - channelCharge);
                            if (channelCharge > 0) {
                                chargeEntity.setChannelChargeMemo(channelConfig
                                        .getRateType()
                                        .getDescription(channelConfig.getRate())
                                        + "收取:"
                                        + TransCharge.format(channelCharge) + "元");

                                comment.set(order.getChannelName()
                                        + ";平台服务费:"
                                        + chargeEntity.getChargeMemo()
                                        + ";渠道服务费:" + chargeEntity.getChannelChargeMemo());
                            }
                        });
                //平台总归集账户上帐
                accountDeposit(order.getId(),
                        accountService.MASTER_ACCOUNT_NO,
                        merchantService.MASTER_MERCHANT_ID,
                        realCharge.get(),
                        TransType.CHARGE,
                        order.getCurrency(),
                        comment.get());
            };

            //代理收费
            if (agentCharge > 0) {
                /*----------代理收费----------------*/
                //交易商户下账（代理商收费）
                accountWithdraw(order.getId(),
                        account.getAccountNo(),
                        merchant.getId(),
                        agentCharge,
                        TransType.AGENT_CHARGE,
                        order.getCurrency(),
                        order.getChannelName() + ";服务费:" + chargeEntity.getAgentChargeMemo());

                //先代理收费上帐
                agentChargeJobs.forEach(Runnable::run);
                /*----------平台收费----------------*/
                if (charge > 0) {
                    masterDeposit.run();
                }
            } else if (charge > 0) {
                //没有收取代理费,则只进行平台收费
                //交易商户下账（平台收费）
                accountWithdraw(order.getId(),
                        account.getAccountNo(),
                        merchant.getId(),
                        charge,
                        TransType.CHARGE,
                        order.getCurrency(),
                        order.getChannelName() + "服务费:" + chargeEntity.getChargeMemo());
                //先代理收费上帐
                agentChargeJobs.forEach(Runnable::run);
                //平台上账
                masterDeposit.run();
            } else {
                //代理收费上帐
                agentChargeJobs.forEach(Runnable::run);
            }
            if (chargeEntity.getCharge() > 0 || chargeEntity.getAgentCharge() > 0) {
                chargeDao.insert(chargeEntity);
            }
        } catch (Throwable e) {
            log.error("处理支付结果失败,paymentId:{}", order.getId(), e);
            throw e;
        }
    }

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentEvent(PaymentCompleteEvent event) {
        PaymentOrder order = event.getOrder();
        if (order.getStatus() == PaymentStatus.success) {
            //只有网关,快捷,代扣 需要进行上帐
            if (order.getTransType().in(TransType.GATEWAY, TransType.QUICK, TransType.WITHHOLD)) {
                handlePaymentComplete(order);
            }
        }
    }


    /**
     * 根据商户ID查询商户资金账户
     *
     * @param merchant
     * @return
     */
    private Account queryAccountByMerchant(String merchant) {
        AccountQueryRequest accountQueryRequest = new AccountQueryRequest();
        accountQueryRequest.setMerchantId(merchant);
        accountQueryRequest.setRequestId(IDGenerator.SNOW_FLAKE_STRING.generate());
        AccountQueryResponse accountQueryResponse = accountService.queryAccount(accountQueryRequest);
        accountQueryResponse.assertSuccess();
        return accountQueryResponse.getAccount();
    }


    /**
     * 资金账户上账
     *
     * @param accountNo   资金账户号
     * @param merchantId  商户ID
     * @param transAmount 交易金额
     * @param transType   交易类型
     * @param currency    交易币种
     */
    private void accountDeposit(String orderId, String accountNo,
                                String merchantId,
                                Long transAmount,
                                TransType transType,
                                CurrencyEnum currency, String memo) {
        AccountTransRequest request = new AccountTransRequest();
        request.setAccountNo(accountNo);
        request.setRequestId(IDGenerator.SNOW_FLAKE_STRING.generate());
        request.setTransAmount(transAmount);
        request.setTransType(transType);
        request.setCurrency(currency);
        request.setComment(memo);
        request.setMerchantId(merchantId);
        request.setPaymentId(orderId);

        //给商户上账
        AccountTransResponse depositResponse = accountTransService.deposit(request);
        depositResponse.assertSuccess();
    }

    /**
     * 资金账户下账
     *
     * @param accountNo   资金账户号
     * @param merchantId  商户ID
     * @param transAmount 交易金额
     * @param transType   交易类型
     * @param currency    交易币种
     */
    private void accountWithdraw(String orderId,
                                 String accountNo,
                                 String merchantId,
                                 Long transAmount,
                                 TransType transType,
                                 CurrencyEnum currency, String memo) {
        AccountTransRequest request = new AccountTransRequest();
        request.setAccountNo(accountNo);
        request.setRequestId(IDGenerator.SNOW_FLAKE_STRING.generate());
        request.setTransAmount(transAmount);
        request.setTransType(transType);
        request.setCurrency(currency);
        request.setComment(memo);
        request.setMerchantId(merchantId);
        request.setPaymentId(orderId);
        //给商户上账
        AccountTransResponse depositResponse = accountTransService.withdraw(request);
        depositResponse.assertSuccess();
    }
}
