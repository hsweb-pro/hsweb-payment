package org.hswebframework.payment.merchant.service.impl;


import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.api.account.AccountFreezeService;
import org.hswebframework.payment.api.account.AccountTransService;
import org.hswebframework.payment.api.account.reqeust.AccountFreezeRequest;
import org.hswebframework.payment.api.account.reqeust.AccountTransRequest;
import org.hswebframework.payment.api.account.reqeust.AccountUnfreezeRequest;
import org.hswebframework.payment.api.charge.ChargeService;
import org.hswebframework.payment.api.enums.*;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.payment.api.merchant.MerchantSubstituteResponse;
import org.hswebframework.payment.api.merchant.SubstituteService;
import org.hswebframework.payment.api.merchant.config.MerchantChannelConfig;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.request.MerchantSubstituteRequest;
import org.hswebframework.payment.api.payment.ChannelConfig;
import org.hswebframework.payment.api.payment.ChannelConfigManager;
import org.hswebframework.payment.api.payment.ConfigurablePaymentChannel;
import org.hswebframework.payment.api.payment.PaymentService;
import org.hswebframework.payment.api.payment.events.SubstitutePaymentCompleteEvent;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.api.payment.substitute.SubstituteChannel;
import org.hswebframework.payment.api.payment.substitute.SubstituteDetailCompleteEvent;
import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.payment.substitute.request.SubstituteRequest;
import org.hswebframework.payment.api.payment.substitute.response.SubstituteResponse;
import org.hswebframework.payment.api.settle.channel.ChannelSettleService;
import org.hswebframework.payment.api.settle.channel.ChannelWithdrawRequest;
import org.hswebframework.payment.api.settle.channel.QueryMerchantSettleRequest;
import org.hswebframework.payment.api.settle.channel.QueryMerchantSettleResponse;
import org.hswebframework.payment.merchant.dao.MerchantChargeDao;
import org.hswebframework.payment.merchant.dao.SubstituteDao;
import org.hswebframework.payment.merchant.dao.SubstituteDetailDao;
import org.hswebframework.payment.merchant.entity.MerchantChargeEntity;
import org.hswebframework.payment.merchant.entity.SubstituteDetailEntity;
import org.hswebframework.payment.merchant.entity.SubstituteEntity;
import org.hswebframework.payment.payment.notify.Notification;
import org.hswebframework.payment.payment.notify.PaymentNotifier;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.hswebframework.payment.api.account.AccountService.MASTER_ACCOUNT_NO;
import static org.hswebframework.payment.api.merchant.MerchantService.MASTER_MERCHANT_ID;
import static org.hswebframework.web.id.IDGenerator.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j(topic = "system.merchant.substitute")
public class LocalSubstituteService extends GenericEntityService<SubstituteEntity, String> implements SubstituteService {

    @Autowired
    private SubstituteDao substituteDao;

    @Autowired
    private SubstituteDetailDao detailDao;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AccountTransService accountTransService;

    @Autowired
    private AccountFreezeService freezeService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ChargeService chargeService;

    @Autowired
    private MerchantChargeDao chargeDao;

    @Autowired
    private ChannelConfigManager channelConfigManager;

    @Autowired
    private ChannelSettleService channelSettleService;

    @Autowired
    private PaymentNotifier paymentNotifier;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public CrudDao<SubstituteEntity, String> getDao() {
        return substituteDao;
    }

    public List<SubstituteDetailEntity> selectDetailBySubstituteId(String substituteId, QueryParamEntity entity) {
        if (StringUtils.isEmpty(substituteId)) {
            return new ArrayList<>();
        }
        return entity.toNestQuery(query -> query.where(SubstituteDetailEntity::getSubstituteId, substituteId))
                .noPaging()
                .execute(detailDao::query);

    }

    public List<SubstituteDetailEntity> selectDetailBySubstituteIdAndMerchantId(String substituteId, String merchantid, QueryParamEntity entity) {
        if (StringUtils.isEmpty(substituteId)) {
            return new ArrayList<>();
        }
        return entity.toNestQuery(query -> query
                .where(SubstituteDetailEntity::getSubstituteId, substituteId)
                .and(SubstituteDetailEntity::getMerchantId, merchantid))
                .noPaging()
                .execute(detailDao::query);

    }

    protected boolean outOfMerchantSubstituteBalance(String merchantId, long amount, String configId) {
        //判断金额是否超出了可代付余额

        //商户的渠道结算资金
        QueryMerchantSettleResponse settle = channelSettleService.queryMerchantSettle(QueryMerchantSettleRequest.builder()
                .merchantId(merchantId)
                .channelId(configId)
                .build());

        settle.assertSuccess();
        if (amount > settle.getAmount()) {
            return true;
        }
        //查询代付中的申请
        long processingAmount = QueryParamEntity.newQuery()
                .where(SubstituteDetailEntity::getMerchantId, merchantId)
                .and(SubstituteDetailEntity::getStatus, SubstituteDetailStatus.PROCESSING)
                .execute(detailDao::sumAmount);

        return amount > settle.getAmount() - processingAmount;
    }

    @Override
    public MerchantSubstituteResponse requestSubstitute(MerchantSubstituteRequest request) {
        MerchantSubstituteResponse substituteResponse = new MerchantSubstituteResponse();

        //获取所有可代付配置
        List<ChannelConfig> channelConfigList = paymentService.getAllChannel()
                .stream()
                .filter(SubstituteChannel.class::isInstance)
                .map(SubstituteChannel.class::cast)
                .filter(channel -> channel.getPayeeType() == request.getPayeeType())
                .map(ConfigurablePaymentChannel.class::cast)
                .map(channel -> {
                    try {
                        return channel.getConfigurator().getPaymentConfigByMerchantId(request.getMerchantId(), TransType.SUBSTITUTE, request.getTotalAmount());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (channelConfigList.isEmpty()) {
            throw ErrorCode.CHANNEL_UNSUPPORTED.createException("没有可用的代付渠道");
        }
        //选择能代付的余额
        ChannelConfig channelConfig = channelConfigList
                .stream()
                .filter(config -> !outOfMerchantSubstituteBalance(request.getMerchantId(), request.getTotalAmount(), config.getId()))
                .findFirst()
                .orElseThrow(() -> ErrorCode.INSUFFICIENT_BALANCE.createException("可代付余额不足"));

        String merchantAccountNo = getMerchantAccountNo(request.getMerchantId());

        String paymentId = SNOW_FLAKE_STRING.generate();

        MerchantChargeEntity chargeEntity = new MerchantChargeEntity();
        chargeEntity.setId(SNOW_FLAKE_STRING.generate());
        chargeEntity.setSettle(false);
        chargeEntity.setCharge(0L);
        chargeEntity.setAmount(request.getTotalAmount());
        chargeEntity.setAgentCharge(0L);
        chargeEntity.setPaymentId(paymentId);
        chargeEntity.setMemo(request.getRemark());
        chargeEntity.setChannelCharge(0L);

        //创建记录
        SubstituteEntity substituteEntity = new SubstituteEntity();
        substituteEntity.setId(SNOW_FLAKE_STRING.generate());
        substituteEntity.setMerchantId(request.getMerchantId());
        substituteEntity.setMerchantName(request.getMerchantName());
        substituteEntity.setCreateTime(new Date());
        substituteEntity.setTransNo(request.getTransNo());
        substituteEntity.setRemark(request.getRemark());
        substituteEntity.setNotifyUrl(request.getNotifyUrl());
        substituteEntity.setStatus(SubstituteStatus.PROCESSING);
        substituteEntity.setTotalAmount(request.getTotalAmount());
        substituteEntity.setPayeeType(request.getPayeeType());
        substituteEntity.setPaymentId(paymentId);
        substituteEntity.setTotal(request.getDetail().size());
        substituteEntity.setTotalSuccess(0);
        substituteEntity.setRealCharge(0L);
        substituteEntity.setRealAmount(0L);
        AtomicLong totalCharge = new AtomicLong(0);

        List<SubstituteDetailEntity> detailEntities = request.getDetail()
                .stream()
                .map(detail -> {
                    SubstituteDetailEntity detailEntity = new SubstituteDetailEntity();
                    detailEntity.setId(SNOW_FLAKE_STRING.generate());
                    detailEntity.setMerchantId(request.getMerchantId());
                    detailEntity.setMerchantName(request.getMerchantName());
                    detailEntity.setSubstituteId(substituteEntity.getId());
                    detailEntity.setAmount(detail.getAmount());
                    detailEntity.setPayee(detail.getPayee().getPayee());
                    detailEntity.setTransNo(detail.getTransNo());
                    detailEntity.setPayeeName(detail.getPayee().getPayeeName());
                    detailEntity.setPayeeInfoJson(JSON.toJSONString(detail.getPayee().toMap()));
                    detailEntity.setPaymentId(paymentId);
                    detailEntity.setChargeAmount(0L);
                    detailEntity.setStatus(SubstituteDetailStatus.PROCESSING);
                    //计算收费
                    chargeService.calculate(request.getMerchantId(),
                            TransType.SUBSTITUTE,
                            channelConfig.getChannel(),
                            request.getTotalAmount(),
                            false,
                            charge -> {
                                //平台收费
                                detailEntity.setChargeAmount(charge.getCharge());
                                detailEntity.setChargeMemo(charge.toString());
                                totalCharge.addAndGet(charge.getCharge());
                            },
                            charge -> {
                                //对商户的收费就是平台收费
                            },
                            (agent, agentCharge, realCharge, parentCharge) -> {
                                //代付代理不收费
                                return null;
                            });

                    detail.setId(detailEntity.getId());
                    return detailEntity;
                })
                .collect(Collectors.toList());

        substituteEntity.setCharge(totalCharge.get());

        //冻结账户
        AccountFreezeRequest freezeRequest = new AccountFreezeRequest();
        freezeRequest.setAccountNo(merchantAccountNo);
        freezeRequest.setMerchantId(request.getMerchantId());
        freezeRequest.setTransType(TransType.SUBSTITUTE);
        //冻结交易金额+手续费
        freezeRequest.setAmount(substituteEntity.getTotalAmount() + substituteEntity.getCharge());
        freezeRequest.setPaymentId(paymentId);
        freezeRequest.setComment("代付冻结");
        freezeService.freeze(freezeRequest).assertSuccess();

        //发起代付
        SubstituteRequest<? extends Payee> substituteRequest = new SubstituteRequest<>();
        substituteRequest.setPaymentId(paymentId);
        substituteRequest.setChannel(channelConfig.getChannel());
        substituteRequest.setChannelId(channelConfig.getId());
        substituteRequest.setOrderId(substituteEntity.getId());
        substituteRequest.setDetails(new ArrayList(request.getDetail()));
        substituteRequest.setProductId(substituteEntity.getId());
        substituteRequest.setMerchantName(request.getMerchantName());
        substituteRequest.setMerchantId(request.getMerchantId());
        substituteRequest.setRemark(request.getRemark());
        substituteRequest.setProductName("代付");
        substituteRequest.setAmount(request.getTotalAmount());
        //不由支付服务进行通知
        substituteRequest.setNotifyType(NotifyType.NONE);
        substituteRequest.setPayeeType(request.getPayeeType());

        //添加记录
        Try.run(() -> substituteDao.insert(substituteEntity))
                .recover(DuplicateKeyException.class, (e) -> {
                    throw ErrorCode.DUPLICATE_REQUEST.createException("存在重复的代付请求");
                });

        detailEntities.forEach(detail -> {
            detail.setPaymentId(paymentId);
            Try.run(() -> detailDao.insert(detail))
                    .recover(DuplicateKeyException.class, (e) -> {
                        throw ErrorCode.DUPLICATE_REQUEST.createException("存在重复的代付明细");
                    });
        });
        //发起代付请求
        paymentService
                .substitute()
                .requestSubstitute(substituteRequest).assertSuccess();

        substituteResponse.setTransId(substituteEntity.getId());
        substituteResponse.setSuccess(true);
        return substituteResponse;
    }

    public String getMerchantAccountNo(String merchantId) {
        return Optional.ofNullable(merchantService.getMerchantById(merchantId))
                .map(Merchant::getAccountNo)
                .orElseThrow(ErrorCode.MERCHANT_NOT_EXISTS::createException);

    }

    //代付单个详情
    @EventListener
    public void handleSubstituteRequest(SubstituteDetailCompleteEvent event) {
        SubstituteDetailStatus status = event.isSuccess() ? SubstituteDetailStatus.SUCCESS : SubstituteDetailStatus.FAIL;

        //更新状态
        DefaultDSLUpdateService.createUpdate(detailDao)
                .set(SubstituteDetailEntity::getStatus, status)
                .set(SubstituteDetailEntity::getRemark, event.getMemo())
                .where(SubstituteDetailEntity::getId, event.getDetailId())
                .and(SubstituteDetailEntity::getStatus, SubstituteDetailStatus.PROCESSING)
                .exec();
    }

    //代付完成
    @EventListener
    public void handleSubstituteRequest(SubstitutePaymentCompleteEvent event) {
        PaymentOrder order = event.getOrder();
        SubstituteStatus status = order.getStatus() == PaymentStatus.success ? SubstituteStatus.SUCCESS : SubstituteStatus.FAIL;
        SubstituteEntity entity = DefaultDSLQueryService
                .createQuery(substituteDao)
                .where(SubstituteEntity::getPaymentId, order.getId())
                .forUpdate()
                .single();
        if (entity == null) {
            log.error("代付信息不存在.paymentId:{}", order.getId());
            return;
        }
        if (entity.getStatus() != SubstituteStatus.PROCESSING) {
            log.warn("重复的代付结果通知:paymentId:{}, status: 订单状态:{},当前代付状态:{}", order.getId(), order.getStatus(), entity.getStatus());
            return;
        }
        if (status != SubstituteStatus.SUCCESS) {
            //明细更新为失败
            DefaultDSLUpdateService.createUpdate(detailDao)
                    .set(SubstituteDetailEntity::getStatus, SubstituteDetailStatus.FAIL)
                    .set(SubstituteDetailEntity::getRemark, event.getOrder().getComment())
                    .where(SubstituteDetailEntity::getSubstituteId, entity.getId())
                    .and(SubstituteDetailEntity::getStatus, SubstituteDetailStatus.PROCESSING)
                    .exec();
        }
        List<SubstituteDetailEntity> allDetail = DefaultDSLQueryService
                .createQuery(detailDao)
                .where(SubstituteDetailEntity::getSubstituteId, entity.getId())
                .listNoPaging();

        boolean hasProcessing = allDetail.stream()
                .anyMatch(detail -> detail.getStatus() == SubstituteDetailStatus.PROCESSING);
        if (status == SubstituteStatus.SUCCESS &&
                hasProcessing) {
            //如果有成功通知,但是还有订单在处理中..
            throw ErrorCode.BUSINESS_FAILED
                    .createException("代付通知成功,但还有明细全部完成.代付结果,批次ID:" + entity.getId());
        }

        //成功笔数
        List<SubstituteDetailEntity> successDetail = DefaultDSLQueryService
                .createQuery(detailDao)
                .where(SubstituteDetailEntity::getSubstituteId, entity.getId())
                .listNoPaging()
                .stream()
                .filter(detail -> detail.getStatus() == SubstituteDetailStatus.SUCCESS)
                .collect(Collectors.toList());

        //总收费
        long realAmount = 0;
        //总收费
        long totalCharge = 0;
        //渠道收费
        long channelCharge = 0;

        for (SubstituteDetailEntity detailEntity : successDetail) {
            realAmount += detailEntity.getAmount();
            totalCharge += detailEntity.getChargeAmount();
            channelCharge += channelConfigManager
                    .getChannelConfigById(order.getChannelId(), ChannelConfig.class)
                    .map(config -> config.calculateRate(detailEntity.getAmount()))
                    .orElse(0L);
        }
        //总计成功笔数
        long totalSuccess = successDetail.size();

        entity.setStatus(status);
        entity.setCompleteTime(order.getCompleteTime());
        entity.setRealAmount(order.getRealAmount());
        entity.setRealCharge(totalCharge);
        entity.setTotalSuccess(Long.valueOf(totalSuccess).intValue());

        //更新代付信息
        DefaultDSLUpdateService.createUpdate(substituteDao)
                .set(entity::getStatus)
                .set(entity::getCompleteTime)
                .set(entity::getRealAmount)
                .set(entity::getRealCharge)
                .set(entity::getTotalSuccess)
                .where(entity::getId)
                .exec();
        //解冻
        String merchantAccountNo = getMerchantAccountNo(entity.getMerchantId());
        AccountUnfreezeRequest freezeRequest = new AccountUnfreezeRequest();
        freezeRequest.setAccountNo(merchantAccountNo);
        freezeRequest.setPaymentId(entity.getPaymentId());
        freezeRequest.setUnfreezeComment("代付" + status.getText());
        freezeService.unfreeze(freezeRequest).assertSuccess();

        if (status == SubstituteStatus.SUCCESS) {
            //下帐
            if (realAmount > 0) {
                AccountTransRequest request = new AccountTransRequest();
                request.setTransAmount(realAmount);
                request.setAccountNo(merchantAccountNo);
                request.setAccountName(order.getMerchantName());
                request.setComment("代付:总计" + allDetail.size() + "笔,成功" + totalSuccess + "笔.");
                request.setCurrency(CurrencyEnum.CNY);
                request.setTransType(TransType.SUBSTITUTE);
                request.setPaymentId(order.getId());
                request.setMerchantId(order.getMerchantId());
                accountTransService.withdraw(request).assertSuccess();
            }
            //手续费
            if (totalCharge > 0) {
                //商户下帐
                {
                    AccountTransRequest request = new AccountTransRequest();
                    request.setTransAmount(totalCharge);
                    request.setAccountNo(merchantAccountNo);
                    request.setAccountName(order.getMerchantName());
                    request.setComment("代付服务费");
                    request.setCurrency(CurrencyEnum.CNY);
                    request.setTransType(TransType.CHARGE);
                    request.setPaymentId(order.getId());
                    request.setMerchantId(order.getMerchantId());
                    accountTransService.withdraw(request).assertSuccess();
                }

                if (totalCharge - channelCharge > 0) {
                    //平台上帐
                    {
                        AccountTransRequest request = new AccountTransRequest();
                        request.setTransAmount(totalCharge - channelCharge);
                        request.setAccountNo(MASTER_ACCOUNT_NO);
                        request.setAccountName("归集资金账户");
                        request.setComment("代付服务费;平台收费:"
                                + TransRateType.TransCharge.format(totalCharge)
                                + "元;渠道收费:" + TransRateType.TransCharge.format(channelCharge) + "元");
                        request.setCurrency(CurrencyEnum.CNY);
                        request.setTransType(TransType.CHARGE);
                        request.setMerchantId(MASTER_MERCHANT_ID);
                        request.setPaymentId(order.getId());
                    }
                    MerchantChargeEntity chargeEntity = new MerchantChargeEntity();
                    chargeEntity.setId(IDGenerator.SNOW_FLAKE_STRING.generate());
                    chargeEntity.setAmount(order.getAmount());
                    chargeEntity.setChannelCharge(channelCharge);
                    chargeEntity.setChannelChargeMemo("代付服务费");
                    chargeEntity.setCharge(totalCharge);
                    chargeEntity.setChargeMemo("代付服务费");
                    chargeEntity.setTransType(TransType.SUBSTITUTE);
                    chargeEntity.setMerchantId(order.getMerchantId());
                    chargeEntity.setChargeCalculated(true);

                    chargeEntity.setPaymentId(order.getId());
                    chargeEntity.setSettle(true);
                    chargeEntity.setSettleTime(new Date());
                    chargeEntity.setAgentCharge(0L);
                    chargeDao.insert(chargeEntity);
                }
            }
            //渠道下账
            ChannelWithdrawRequest request = ChannelWithdrawRequest.builder()
                    .amount(realAmount + channelCharge)//交易金额+渠道费率
                    .channelId(order.getChannelId())
                    .paymentId(order.getId())
                    .channel(order.getChannel())
                    .merchantId(order.getMerchantId())
                    .merchantName(order.getMerchantName())
                    .transType(order.getTransType())
                    .memo("代付:成功"
                            + totalSuccess + "笔,总计:" + allDetail.size()
                            + "笔,渠道服务费:"
                            + TransRateType.TransCharge.format(channelCharge)
                            + "元")
                    .build();

            channelSettleService.withdraw(request).assertSuccess();
        }
        //发起通知
        long settleAmount = realAmount;
        if (StringUtils.hasText(entity.getNotifyUrl())) {
            Runnable doNotify = () -> {
                Notification notification = FastBeanCopier.copy(order, new Notification());
                Map<String, Object> notifyConfig = new HashMap<>();
                notifyConfig.put("notifyUrl", entity.getNotifyUrl());

                notification.setNotifyConfig(notifyConfig);
                notification.setPaymentId(order.getId());
                Map<String, String> extraParam = new TreeMap<>();
                extraParam.put("settleAmount", String.valueOf(settleAmount));
                extraParam.put("totalSuccess", String.valueOf(totalSuccess));
                extraParam.put("total", String.valueOf(allDetail.size()));
                extraParam.put("transferId", entity.getId());
                extraParam.put("transNo", entity.getTransNo());
                List<Map<String, Object>> details = allDetail.stream()
                        .map(detail -> {
                            Map<String, Object> detailMap = new TreeMap<>();
                            detailMap.putAll(JSON.parseObject(detail.getPayeeInfoJson()));
                            detailMap.put("transNo", detail.getTransNo());
                            detailMap.put("amount", detail.getAmount());
                            detailMap.put("status", detail.getStatus().getValue());
                            detailMap.put("statusText", detail.getStatus().getText());
                            detailMap.put("remark", detail.getRemark());
                            return detailMap;
                        }).collect(Collectors.toList());

                extraParam.put("details", JSON.toJSONString(details));
                notification.setExtraParam(extraParam);
                paymentNotifier.doNotify(NotifyType.HTTP, notification);
            };
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        doNotify.run();
                    }
                });
            } else {
                doNotify.run();
            }
        }
    }
}
