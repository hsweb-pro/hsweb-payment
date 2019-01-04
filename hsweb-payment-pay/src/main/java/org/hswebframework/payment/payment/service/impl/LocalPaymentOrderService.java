package org.hswebframework.payment.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.NotifyType;
import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.payment.dao.PaymentOrderDao;
import org.hswebframework.payment.payment.entity.PaymentOrderEntity;
import org.hswebframework.payment.payment.events.ChannelNotificationEvent;
import org.hswebframework.payment.payment.events.NotificationSuccessEvent;
import org.hswebframework.payment.payment.events.PaymentRequestAfterEvent;
import org.hswebframework.payment.payment.events.PaymentRequestBeforeEvent;
import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import org.hswebframework.payment.payment.notify.Notification;
import org.hswebframework.payment.payment.notify.PaymentNotifier;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.validator.group.CreateGroup;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
@Slf4j(topic = "system.payment.order")
public class LocalPaymentOrderService extends GenericEntityService<PaymentOrderEntity, String>
        implements org.hswebframework.payment.payment.service.LocalPaymentOrderService, org.hswebframework.payment.api.payment.order.PaymentOrderService {

    @Autowired
    private PaymentOrderDao paymentOrderDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Autowired
    private PaymentNotifier paymentNotifier;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    @SuppressWarnings("all")
    public void handlePaymentRequestEvent(PaymentRequestBeforeEvent event) {
        PaymentOrderEntity entity = createEntity();
        entity.copyFrom(event.getRequest());
        entity.setId(event.getPaymentId());
        entity.setTransType(event.getTransType());
        entity.setNotified(false);
        entity.setChannelProvider(event.getPaymentChannel().getChannelProvider());
        entity.setChannelProviderName(event.getPaymentChannel().getChannelProviderName());
        entity.setCreateTime(new Date());
        entity.setRealAmount(0L);
        entity.setStatus(PaymentStatus.prepare);
        entity.setRequestJson(JSON.toJSONString(event.getRequest(), SerializerFeature.WriteClassName));
        entity.setChannelName(event.getPaymentChannel().getChannelName());
        //处理参数校验失败
        Try.run(() -> entity.tryValidate(CreateGroup.class))
                .recover(e -> {
                    throw ErrorCode.ILLEGAL_PARAMETERS.createException(e);
                })
                .get();

        //处理重复支付
        Try.run(() -> getDao().insert(entity))
                .recover(DuplicateKeyException.class, e -> {
                    throw ErrorCode.DUPLICATE_PAYMENT.createException(e);
                })
                .get();
    }

    @Override
    public List<PaymentOrderEntity> queryPayingOrder(Date createTimeLt, int pageSize) {
        return createQuery()
                .where()
                .in("status", PaymentStatus.paying)
                .lt("createTime", createTimeLt)
                .list(0, pageSize);
    }

    @Override
    public int updateTimeoutStatus(List<String> paymentIdList) {
        if (CollectionUtils.isEmpty(paymentIdList)) {
            return 0;
        }
        return createUpdate().where()
                .set("status", PaymentStatus.timeout)
                .in("id", paymentIdList)
                .and("status", PaymentStatus.paying)
                .exec();
    }

    @Override
    public PagerResult<PaymentOrderEntity> queryMerchantOrder(QueryParamEntity entity) {
        return selectPager(entity);
    }

    @Override
    public PaymentOrderEntity queryOrderByMerchantIdAndOrderId(String merchantId, String orderId) {
        Objects.requireNonNull(merchantId, "商户ID为空");
        Objects.requireNonNull(orderId, "订单ID为空");
        return createQuery().where("merchantId", merchantId).and("id", orderId).single();
    }

    @Override
    public List<PaymentOrderEntity> queryAgentSingleMerchantOrder(String agentId, String merchantId) {
        Objects.requireNonNull(agentId, "代理商ID为空");
        Objects.requireNonNull(merchantId, "商户ID为空");
        return createQuery()
                .where("merchantId$agent", agentId)
                .and("merchantId", merchantId).listNoPaging();
    }

    @Override
    public PagerResult<PaymentOrderEntity> queryAgentAllMerchantOrder(String agentId, QueryParamEntity paramEntity) {
        return paramEntity.toNestQuery()
                .and("merchantId$agent$children", agentId)
                .execute(this::selectPager);
    }

    @Override
    public PaymentOrderEntity queryOrderByIdAndMerchantId(String merchantId, String orderId) {
        return createQuery().where("id", orderId).and("merchantId$agent$children", merchantId).single();
    }


    @EventListener
    public void handlePaymentAfterEvent(PaymentRequestAfterEvent event) {
        int total = createUpdate()
                .set("responseJson", JSON.toJSONString(event.getResponse()))
                .set("channelId", event.getResponse().getChannelId())
                .set("status", event.getResponse().isSuccess() ? PaymentStatus.paying : PaymentStatus.requestFail)
                .where("id", event.getPaymentId())
                .and("status", PaymentStatus.prepare)
                .exec();
        if (total == 0) {
            createUpdate()
                    .set("responseJson", JSON.toJSONString(event.getResponse()))
                    .set("channelId", event.getResponse().getChannelId())
                    .where("id", event.getPaymentId())
                    .exec();
        }
    }


    /**
     * 处理渠道通知事件
     *
     * @param event 事件对象
     */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleChannelNotificationEvent(ChannelNotificationEvent event) {
        try (MDC.MDCCloseable closeable = MDC.putCloseable("businessId", event.getResult().getPaymentId())) {
            Date now = new Date();
            //更新支付状态
            ChannelNotificationResult result = event.getResult();
            String paymentId = result.getPaymentId();
            PaymentOrderEntity order = createQuery()
                    .where(PaymentOrderEntity::getId, paymentId)
                    .forUpdate()
                    .single();
            if (null == order) {
                log.warn("支付订单[{}]不存在.事件:{}", paymentId, JSON.toJSONString(result));
                return;
            }
            if (order.getStatus() == PaymentStatus.success
                    || order.getStatus() == PaymentStatus.fail) {
                //订单状态失败,通知结果为成功？
                if (order.getStatus() == PaymentStatus.fail && result.isSuccess()) {
                    log.warn("订单[{}]状态为支付失败,渠道通知支付成功!", order.getId());
                } else {
                    log.error("重复的渠道通知,订单id:{}", paymentId);
                    return;
                }
            }
            //拿到原始请求
            PaymentRequest request = order.getOriginalRequest(PaymentRequest.class);
            order.setUpdateTime(now);
            order.setCompleteTime(now);
            order.setStatus(result.isSuccess() ? PaymentStatus.success : PaymentStatus.fail);
            order.setComment(result.getMemo());
            order.setRealAmount(result.getAmount());
            try {
                if (null != result.getResultObject()) {
                    order.setChannelResult(JSON.toJSONString(result.getResultObject()));
                }
            } catch (Exception e) {
                log.error("转换渠道返回结果失败:{}", result.getResultObject(), e);
            }
            //推送支付完成事件
            eventPublisher.publishEvent(order.getTransType().createCompleteEvent(order.copyTo(new PaymentOrder())));
            //更新支付订单状态
            createUpdate()
                    .set(order::getStatus)
                    .set(order::getUpdateTime)
                    .set(order::getCompleteTime)
                    .set(order::getComment)
                    .set(order::getRealAmount)
                    .set(order::getChannelResult)
                    .where()
                    .is(order::getId)
                    .exec();
            //通知商户
            if (request.getNotifyType() != NotifyType.NONE) {
                Runnable doNotify = () -> {
                    Notification notification = order.copyTo(new Notification());
                    notification.setNotifyConfig(request.getNotifyConfig());
                    notification.setPaymentId(paymentId);
                    Map<String, String> extraParam = new TreeMap<>();
                    if (request.getExtraParam() != null) {
                        extraParam.putAll(request.getExtraParam());
                    }
                    extraParam.put("settleAmount", String.valueOf(order.getRealAmount()));
                    notification.setExtraParam(extraParam);
                    paymentNotifier.doNotify(request.getNotifyType(), notification);
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
        } catch (Throwable e) {
            log.error("处理支付通知失败,paymentId={}", event.getResult(), e);
            throw e;
        }

    }

    /**
     * 处理商户通知成功事件
     *
     * @param event 事件对象
     */
    @EventListener
    public void handleNotificationSuccessEvent(NotificationSuccessEvent event) {
        String paymentId = event.getPaymentId();
        Date now = new Date();
        //更新通知状态
        createUpdate()
                .set("notified", true)
                .set("notifyTime", now)
                .set("updateTime", now)
                .where("id", paymentId)
                .exec();
    }

    @Override
    public String insert(PaymentOrderEntity entity) {
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        entity.setNotified(false);
        entity.setStatus(PaymentStatus.paying);
        return super.insert(entity);
    }

    @Override
    public int updateByPk(String id, PaymentOrderEntity entity) {
        entity.setUpdateTime(new Date());
        return super.updateByPk(id, entity);
    }

    @Override
    public PaymentOrderDao getDao() {
        return paymentOrderDao;
    }

    @Override
    public PaymentOrder getOrderById(String orderId) {
        return convert(selectByPk(orderId));
    }

    protected PaymentOrder convert(PaymentOrderEntity entity) {
        return Optional.ofNullable(entity)
                .map(e -> e.copyTo(new PaymentOrder()))
                .orElse(null);
    }

    @Override
    public PagerResult<PaymentOrder> query(QueryParamEntity entity) {
        PagerResult<PaymentOrderEntity> result = selectPager(entity);
        return PagerResult.of(result.getTotal(), result
                .getData()
                .stream()
                .map(this::convert).collect(Collectors.toList()));
    }
}
