package org.hswebframework.payment.merchant.service.impl;

import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.api.account.AccountFreezeService;
import org.hswebframework.payment.api.account.AccountService;
import org.hswebframework.payment.api.account.AccountTransService;
import org.hswebframework.payment.api.account.reqeust.AccountFreezeRequest;
import org.hswebframework.payment.api.account.reqeust.AccountTransRequest;
import org.hswebframework.payment.api.account.reqeust.AccountUnfreezeRequest;
import org.hswebframework.payment.api.charge.ChargeService;
import org.hswebframework.payment.api.enums.*;
import org.hswebframework.payment.api.enums.TransRateType.TransCharge;
import org.hswebframework.payment.api.merchant.*;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.config.MerchantSettleConfig;
import org.hswebframework.payment.api.merchant.request.*;
import org.hswebframework.payment.api.merchant.response.*;
import org.hswebframework.payment.api.payment.ChannelConfig;
import org.hswebframework.payment.api.payment.ChannelConfigManager;
import org.hswebframework.payment.api.payment.PaymentResponse;
import org.hswebframework.payment.api.payment.PaymentService;
import org.hswebframework.payment.api.payment.events.WithdrawPaymentCompleteEvent;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.api.payment.order.PaymentOrderService;
import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.payment.withdraw.WithdrawDetail;
import org.hswebframework.payment.api.payment.withdraw.WithdrawPaymentRequest;
import org.hswebframework.payment.api.settle.channel.ChannelSettleInfo;
import org.hswebframework.payment.api.settle.channel.ChannelSettleService;
import org.hswebframework.payment.merchant.dao.MerchantChargeDao;
import org.hswebframework.payment.merchant.dao.MerchantWithdrawDao;
import org.hswebframework.payment.merchant.entity.MerchantChargeEntity;
import org.hswebframework.payment.merchant.entity.MerchantWithdrawEntity;
import org.hswebframework.payment.merchant.service.MerchantWithdrawService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.bean.BeanValidator;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hswebframework.payment.api.account.AccountService.MASTER_ACCOUNT_NO;

/**
 * @author Lind
 * @since 1.0
 */
@Service
@Slf4j(topic = "system.merchant.withdraw")
public class LocalMerchantWithdrawService extends GenericEntityService<MerchantWithdrawEntity, String> implements WithdrawService, MerchantWithdrawService {

    @Autowired
    private MerchantChargeDao chargeDao;

    @Autowired
    private MerchantWithdrawDao merchantWithdrawDao;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private AgentMerchantService agentMerchantService;

    @Autowired
    private AccountTransService accountTransService;

    @Autowired
    private AccountFreezeService freezeService;

    @Autowired
    private ChargeService chargeService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ChannelConfigManager channelConfigManager;

    @Autowired
    private PaymentOrderService paymentOrderService;

    @Autowired
    private ChannelSettleService channelSettleService;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public CrudDao<MerchantWithdrawEntity, String> getDao() {
        return merchantWithdrawDao;
    }


    @Override
    public ApplyWithdrawResponse applyWithdraw(ApplyWithdrawRequest request) {
        BeanValidator.tryValidate(request);
        //商户信息
        MerchantOrAgent merchant = getMerchantOrAgent(request.getMerchantId());

        //计算收费
        MerchantChargeEntity chargeEntity = new MerchantChargeEntity();
        chargeEntity.setId(IDGenerator.SNOW_FLAKE_STRING.generate());
        chargeEntity.setAmount(request.getAmount());
        chargeEntity.setMerchantId(request.getMerchantId());
        chargeEntity.setTransType(TransType.WITHDRAW);
        chargeEntity.setPayTime(new Date());
        chargeEntity.setSettle(false);
        chargeEntity.setCharge(0L);
        chargeEntity.setAgentCharge(0L);
        chargeEntity.setChargeCalculated(false);

        String id = IDGenerator.SNOW_FLAKE_STRING.generate();

        chargeService.calculate(request.getMerchantId(),
                TransType.WITHDRAW,
                null,
                request.getAmount(),
                false, //#11 代理不收取手续费
                chargeEntity::chargeComplete,
                chargeEntity::agentChargeComplete,
                (agent, charge, real, parent) -> {
                    // TODO: 18-12-12
                    return null;
                });

        //总计收费
        long charge = chargeEntity.getTotalCharge();
        //冻结用户余额
        AccountFreezeRequest freezeRequest = new AccountFreezeRequest();
        freezeRequest.setTransType(TransType.WITHDRAW);
        freezeRequest.setMerchantId(merchant.getId());
        freezeRequest.setComment("提现冻结");
        freezeRequest.setAccountNo(merchant.getAccountNo());
        //冻结金额=提现金额+手续费
        freezeRequest.setAmount(charge + request.getAmount());
        freezeRequest.setPaymentId(id);
        freezeService.freeze(freezeRequest).assertSuccess();

        //保存提现申请信息
        MerchantWithdrawEntity withdrawEntity = MerchantWithdrawEntity
                .builder()
                .id(id)
                .merchantId(request.getMerchantId())
                .merchantName(merchant.getName())
                .transAmount(request.getAmount())
                .payeeType(request.getPayeeType())
                .payeeInfoJson(JSON.toJSONString(request.getPayee().toMap()))//收款人信息
                .withdrawType(WithdrawType.MANUAL)
                .chargeAmount(charge)
                .status(WithdrawStatus.APPLYING)
                .applyTime(request.getApplyTime())
                .build();
        getDao().insert(withdrawEntity);

        ApplyWithdrawResponse response = new ApplyWithdrawResponse();
        response.setWithdrawStatus(WithdrawStatus.HANDING);
        response.setSuccess(true);
        return response;
    }

    @Override
    public HandlerWithdrawResponse handleWithdraw(HandlerWithdrawRequest request) {
        BeanValidator.tryValidate(request);
        HandlerWithdrawResponse response = new HandlerWithdrawResponse();
        //查询提现申请信息;
        MerchantWithdrawEntity withdrawEntity = selectByPk(request.getWithdrawId());
        Objects.requireNonNull(withdrawEntity, "未找到提现申请信息");
        if (!withdrawEntity.getStatus().equals(WithdrawStatus.APPLYING)) {
            response.setMessage("该提现申请已处理");
            response.setSuccess(false);
            return response;
        }

        ChannelConfig config = channelConfigManager
                .getChannelConfigById(request.getChannelId(), ChannelConfig.class)
                .orElseThrow(ErrorCode.CHANNEL_UNSUPPORTED::createException);

        long channelCharge = config.calculateRate(withdrawEntity.getTransAmount());

        //计算收费
        MerchantChargeEntity chargeEntity = new MerchantChargeEntity();
        chargeEntity.setId(IDGenerator.SNOW_FLAKE_STRING.generate());
        chargeEntity.setAmount(withdrawEntity.getTransAmount());
        chargeEntity.setMerchantId(withdrawEntity.getMerchantId());
        chargeEntity.setTransType(TransType.WITHDRAW);
        chargeEntity.setPayTime(new Date());
        chargeEntity.setSettle(false);
        chargeEntity.setCharge(0L);
        chargeEntity.setChannelCharge(channelCharge);
        chargeEntity.setAgentCharge(0L);
        chargeEntity.setChannelChargeMemo(channelCharge != 0 ? config.getRateType().getDescription(config.getRate()) : "");
        chargeEntity.setChargeCalculated(false);

        chargeService.calculate(withdrawEntity.getMerchantId(),
                TransType.WITHDRAW,
                null,
                withdrawEntity.getTransAmount(),
                false, //#11 代理不收取手续费
                chargeEntity::chargeComplete,
                chargeEntity::agentChargeComplete,
                (agentMerchant, transCharge, real, parent) -> {
                    // TODO: 18-12-12
                    return null;
                });

        //设置收费
        withdrawEntity.setChargeAmount(chargeEntity.getTotalCharge());

        //渠道资金结算账户
        ChannelSettleInfo info = channelSettleService.getInfoByAccountNo(config.getAccountNo());
        if (info == null) {
            throw ErrorCode.CHANNEL_CONFIG_ERROR.createException("渠道结算账户不存在");
        }
        long transAmount = withdrawEntity.getTransAmount();
        if (info.getBalance() < transAmount + channelCharge) {
            throw ErrorCode.INSUFFICIENT_BALANCE.createException("渠道结算账户余额不足，确认提现申请失败!");
        }

        //发起提现交易
        WithdrawPaymentRequest<? extends Payee> paymentRequest = new WithdrawPaymentRequest<>();
        //收款人
        paymentRequest.setPayee(withdrawEntity.getPayeeInfo());
        paymentRequest.setPayeeType(withdrawEntity.getPayeeType());
        paymentRequest.setOrderId(withdrawEntity.getId());
        paymentRequest.setNotifyType(NotifyType.NONE);
        paymentRequest.setProductId("withdraw");
        paymentRequest.setProductName("提现");
        paymentRequest.setChannel(config.getChannel());
        paymentRequest.setAmount(withdrawEntity.getTransAmount());
        paymentRequest.setMerchantName(withdrawEntity.getMerchantName());
        paymentRequest.setMerchantId(withdrawEntity.getMerchantId());
        paymentRequest.setRemark("提现");
        //指定渠道配置
        paymentRequest.setChannelId(config.getId());
        PaymentResponse paymentResponse = paymentService.withdraw().requestWithdrawPayment(paymentRequest);
        paymentResponse.assertSuccess();

        String paymentId = paymentResponse.getPaymentId();
        chargeEntity.setPaymentId(paymentId);
        //记录收费
        chargeDao.insert(chargeEntity);

        //修改提现申请信息
        createUpdate()
                .set(MerchantWithdrawEntity::getStatus, WithdrawStatus.HANDING)
                .set(MerchantWithdrawEntity::getHandleTime, new Date())
                .set(MerchantWithdrawEntity::getPaymentId, paymentId)
                .set(MerchantWithdrawEntity::getChargeAmount, withdrawEntity.getChargeAmount())
                .where(withdrawEntity::getId)
                .exec();

        response.setSuccess(true);
        response.setMessage("提现申请处理中");
        return response;
    }

    @EventListener
    public void handleWithdrawEvent(WithdrawPaymentCompleteEvent event) {
        PaymentOrder order = event.getOrder();
        //提现完成处理
        if (order.getTransType() == TransType.WITHDRAW) {
            if (order.getStatus() == PaymentStatus.success) {
                CompleteWithdrawRequest request = new CompleteWithdrawRequest();
                request.setWithdrawId(order.getOrderId());
                request.setCompleteProve(order.getComment());
                completeWithdraw(request)
                        .assertSuccess();
            } else if (order.getStatus() == PaymentStatus.fail) {
                closeWithdraw(CloseWithdrawRequest.builder()
                        .withdrawId(order.getOrderId())
                        .comment(order.getComment())
                        .build())
                        .assertSuccess();
            }
        }
    }

    @Override
    public MerchantWithdrawEntity selectByPk(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return createQuery()
                .where(GenericEntity.id, id)
                .forUpdate()
                .single();
    }

    @Override
    public CompleteWithdrawResponse completeWithdraw(CompleteWithdrawRequest request) {
        CompleteWithdrawResponse response = new CompleteWithdrawResponse();

        MerchantWithdrawEntity withdrawEntity = selectByPk(request.getWithdrawId());
        if (!withdrawEntity.getStatus().equals(WithdrawStatus.HANDING)) {
            response.setSuccess(false);
            response.setMessage("提现申请已处理");
            return response;
        }
        MerchantOrAgent merchant = getMerchantOrAgent(withdrawEntity.getMerchantId());

        //查询到收费记录
        MerchantChargeEntity chargeEntity = DefaultDSLQueryService.createQuery(chargeDao)
                .where(MerchantChargeEntity::getPaymentId, withdrawEntity.getPaymentId())
                .single();
        String paymentId = withdrawEntity.getPaymentId();

        PaymentOrder order = paymentOrderService.getOrderById(paymentId);
        //计算渠道手续费
        ChannelConfig config = channelConfigManager.getChannelConfigById(order.getChannelId(), ChannelConfig.class)
                .orElseGet(() -> {
                    logger.error("无法获取支付订单[{}]渠道[{}]配置信息", order.getId(), order.getChannelProviderName());
                    return null;
                });
        long channelCharge = config == null ? 0 : config.calculateRate(withdrawEntity.getTransAmount());
        //渠道虚拟资金账户
        long masterWithdrawAmount = chargeEntity.getCharge() - channelCharge;

        String comment = withdrawEntity.getMerchantName()
                + "提现手续费:(" + TransCharge.format(chargeEntity.getCharge())
                + "-" + TransCharge.format(channelCharge) + ");平台手续费:" + chargeEntity.getChargeMemo()
                + ";渠道手续费:" + (channelCharge == 0 ? "0" : config.getRateType().getDescription(config.getRate()));

        //解冻商户余额
        AccountUnfreezeRequest freezeRequest = new AccountUnfreezeRequest();
        freezeRequest.setAccountNo(merchant.getAccountNo());
        freezeRequest.setPaymentId(withdrawEntity.getId());
        freezeRequest.setUnfreezeComment("提现完成");
        freezeService.unfreeze(freezeRequest).assertSuccess();

        {//商户下帐
            AccountTransRequest transRequest = new AccountTransRequest();
            transRequest.setAccountNo(merchant.getAccountNo());
            transRequest.setTransAmount(withdrawEntity.getTransAmount());
            transRequest.setCurrency(CurrencyEnum.CNY);
            transRequest.setTransType(TransType.WITHDRAW);
            transRequest.setPaymentId(paymentId);
            transRequest.setMerchantId(withdrawEntity.getMerchantId());
            transRequest.setComment("提现");
            accountTransService.withdraw(transRequest).assertSuccess();
            //服务费
            if (chargeEntity.getCharge() > 0) {
                AccountTransRequest chargeTransRequest = new AccountTransRequest();
                chargeTransRequest.setAccountNo(merchant.getAccountNo());
                chargeTransRequest.setTransAmount(chargeEntity.getCharge());
                chargeTransRequest.setCurrency(CurrencyEnum.CNY);
                chargeTransRequest.setTransType(TransType.AGENT_CHARGE);
                chargeTransRequest.setPaymentId(paymentId);
                chargeTransRequest.setMerchantId(withdrawEntity.getMerchantId());
                chargeTransRequest.setComment("提现服务费:" + chargeEntity.getChargeMemo());
                accountTransService.withdraw(chargeTransRequest).assertSuccess();
            }
        }
        //平台收费
        {
            if (masterWithdrawAmount > 0) {
                AccountTransRequest transRequest = new AccountTransRequest();
                transRequest.setAccountNo(MASTER_ACCOUNT_NO);
                transRequest.setTransAmount(masterWithdrawAmount);
                transRequest.setCurrency(CurrencyEnum.CNY);
                transRequest.setTransType(TransType.CHARGE);
                transRequest.setPaymentId(paymentId);
                transRequest.setMerchantId(MASTER_ACCOUNT_NO);
                transRequest.setComment(comment);
                accountTransService.deposit(transRequest).assertSuccess();
            } else if (masterWithdrawAmount < 0) {
                //手续费要倒贴
                AccountTransRequest transRequest = new AccountTransRequest();
                transRequest.setAccountNo(MASTER_ACCOUNT_NO);
                transRequest.setTransAmount(Math.abs(masterWithdrawAmount));
                transRequest.setCurrency(CurrencyEnum.CNY);
                transRequest.setTransType(TransType.CHARGE);
                transRequest.setPaymentId(paymentId);
                transRequest.setMerchantId(MASTER_ACCOUNT_NO);
                transRequest.setComment(comment);
                accountTransService.withdraw(transRequest).assertSuccess();
            }
        }
        chargeEntity.setSettleTime(new Date());
        chargeEntity.setSettle(true);
        //结算收费
        DefaultDSLUpdateService.createUpdate(chargeDao)
                .set(chargeEntity::getSettle)
                .set(chargeEntity::getSettleTime)
                .where("id", chargeEntity.getId())
                .exec();
        //修改提现申请信息
        createUpdate()
                .set(MerchantWithdrawEntity::getStatus, WithdrawStatus.SUCCESS)
                .set(MerchantWithdrawEntity::getCompleteTime, new Date())
                .set(MerchantWithdrawEntity::getCompleteProve, request.getCompleteProve())
                .where(withdrawEntity::getId)
                .exec();
        response.setSuccess(true);
        response.setMessage("提现完成");
        return response;
    }

    @Override
    public CloseWithdrawResponse closeWithdraw(CloseWithdrawRequest request) {
        CloseWithdrawResponse response = new CloseWithdrawResponse();

        MerchantWithdrawEntity withdrawEntity = selectByPk(request.getWithdrawId());

        MerchantOrAgent merchant = getMerchantOrAgent(withdrawEntity.getMerchantId());

        if (!withdrawEntity.getStatus().in(WithdrawStatus.APPLYING, WithdrawStatus.HANDING)) {
            response.setSuccess(false);
            response.setMessage("提现已处理,无法关闭");
            return response;
        }
        //如果是处理中,直接解冻商户余额
//        if (withdrawEntity.getStatus() == WithdrawStatus.HANDING) {
        AccountUnfreezeRequest freezeRequest = new AccountUnfreezeRequest();
        freezeRequest.setAccountNo(merchant.getAccountNo());
        freezeRequest.setPaymentId(withdrawEntity.getId());
        freezeRequest.setUnfreezeComment("关闭提现");
        freezeService.unfreeze(freezeRequest).assertSuccess();
//        }

        //修改提现申请信息
        createUpdate()
                .set(MerchantWithdrawEntity::getStatus, WithdrawStatus.CLOSE)
                .set(MerchantWithdrawEntity::getCloseTime, new Date())
                .set(MerchantWithdrawEntity::getComment, request.getComment())
                .where(withdrawEntity::getId)
                .exec();

        response.setSuccess(true);
        response.setMessage("提现申请已关闭");
        return response;
    }

    @Override
    public QueryWithdrawResponse queryWithdraw(QueryWithdrawRequest request) {
        BeanValidator.tryValidate(request);
        QueryWithdrawResponse response = new QueryWithdrawResponse();
        List<MerchantWithdrawEntity> withdrawLogList = createQuery()
                .where(request::getMerchantId)
                .listNoPaging();

        List<MerchantWithdrawLog> collect = withdrawLogList
                .stream()
                .map(e -> FastBeanCopier.copy(e, new MerchantWithdrawLog()))
                .collect(Collectors.toList());
        response.setWithdrawLogList(collect);
        response.setTotal(collect.size());
        response.setSuccess(true);
        return response;
    }

    @Override
    public MerchantWithdrawEntity queryWithdrawLogByIdAndMerchantId(String id, String merchantId) {
        Assert.hasText(id, "id不能为空");
        Assert.hasText(merchantId, "merchantId不能为空");

        return createQuery().where("id", id).and("merchantId", merchantId).forUpdate().single();
    }

    public MerchantOrAgent getMerchantOrAgent(String id) {
        Object moa = Optional.<Object>ofNullable(merchantService.getMerchantById(id))
                .orElseGet(() -> agentMerchantService.getAgentById(id));
        if (moa == null) {
            throw ErrorCode.MERCHANT_NOT_EXISTS.createException("商户不存在");
        }
        MerchantOrAgent merchantOrAgent = FastBeanCopier.copy(moa, MerchantOrAgent::new);
        if (moa instanceof AgentMerchant) {
            merchantOrAgent.setAgent(true);
        }
        return merchantOrAgent;
    }

    @Getter
    @Setter
    public static class MerchantOrAgent {
        private String name;

        private String id;

        private String accountNo;

        private boolean isAgent;
    }
}
