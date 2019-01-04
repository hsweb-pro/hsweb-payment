package org.hswebframework.payment.payment.channel;

import org.hswebframework.payment.api.payment.*;
import org.hswebframework.payment.payment.entity.PaymentOrderEntity;
import org.hswebframework.payment.payment.events.ChannelNotificationEvent;
import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import org.hswebframework.payment.payment.service.LocalPaymentOrderService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.utils.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 抽象支付渠道类,实现自动获取配置等基础功能
 *
 * @param <C>   配置类型,在执行支付请求时,将自动获取配置并映射为对应的类型
 * @param <REQ> 支付请求类型
 * @param <RES> 支付请求结果类型
 * @author zhouhao
 * @see PaymentRequest
 * @see PaymentResponse
 * @see ConfigurablePaymentChannel
 * @see org.hswebframework.payment.api.payment.PaymentChannelConfigurator
 * @since 1.0.0
 */
@Slf4j(topic = "system.payment.channel")
public abstract class AbstractPaymentChannel<
        C extends ChannelConfig, //渠道的配置信息
        REQ extends PaymentRequest,
        RES extends PaymentResponse>
        implements PaymentChannel<REQ, RES>, ConfigurablePaymentChannel<C> {

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Getter
    @Setter
    @Value("${hsweb.pay.notify.location:http://localhost:8080/}")
    private String notifyLocation;

    @Getter
    @Setter
    @Value("${hsweb.server.location:http://localhost:8080/}")
    private String serverLocation;

    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    @Autowired
    protected LocalPaymentOrderService orderService;

    private volatile PaymentChannelConfigurator<C> configurator;

    private Class<C> configType;

    protected abstract RES doRequestPay(C config, REQ request);

    protected void runLater(Runnable runnable, long delay, TimeUnit timeUnit) {
        scheduledExecutorService.schedule(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, delay, timeUnit);
    }

    @Override
    public RES requestPayment(REQ request) {
        C config;
        if (StringUtils.hasLength(request.getChannelId())) {
            config = getConfigurator().getPaymentConfigById(request.getChannelId());
        } else {
            config = getConfigurator().getPaymentConfigByMerchantId(request.getMerchantId(), getTransType(), request.getAmount());
        }
        if (config != null) {
            log.info("使用渠道配置: {}.{}:{} ", getTransType(), getChannel(), config.getName());
            //统一returnUrl
            if (!StringUtils.isEmpty(request.getReturnUrl())) {
                request.setReturnUrl(getServerLocation(config) + "payment/return/" + request.getPaymentId());
            }
        } else {
            log.warn("未配置的渠道{}.{}", getTransType(), getChannel());
        }
        RES res = doRequestPay(config, request);
        if (config != null) {
            res.setChannelId(config.getId());
        }
        return res;
    }

    /**
     * 根据paymentId获取渠道配置,一般用于在发起支付后的再次请求时获取配置
     *
     * @param paymentId 支付订单ID {@link PaymentOrderEntity#getId()}
     * @return 渠道配置
     */
    protected C getChannelConfig(String paymentId) {
        RES res = getOriginalRequestResponse(paymentId).getResponse();
        if (res == null) {
            return null;
        }
        return configurator.getPaymentConfigById(res.getChannelId());
    }

    protected String getServerLocation(C config) {
        String location = config.getServerLocation();
        if (StringUtils.isEmpty(location)) {
            location = getServerLocation();
        }
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        return location;
    }

    protected String getNotifyLocation(C config) {
        String location = config.getNotifyLocation();
        if (StringUtils.isEmpty(location)) {
            location = getNotifyLocation();
        }
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        return location;
    }

    /**
     * 获取原始支付请求和响应结果
     *
     * @param paymentId 支付订单ID
     * @return 原始支付请求和响应结果
     */
    @SuppressWarnings("unchecked")
    protected OriginalRequestResponse getOriginalRequestResponse(String paymentId) {
        OriginalRequestResponse reqAndRes = new OriginalRequestResponse();
        PaymentOrderEntity order = orderService.selectByPk(paymentId);
        if (null == order) {
            return reqAndRes;
        }
        reqAndRes.request = order.getOriginalRequest(getRequestType());
        reqAndRes.response = order.getOriginalResponse(getResponseType());
        return reqAndRes;
    }

    @Getter
    @Setter
    protected class OriginalRequestResponse {
        REQ request;

        RES response;
    }

    protected void afterHandleChannelNotify(ChannelNotificationResult result) {
        eventPublisher.publishEvent(new ChannelNotificationEvent(result));
    }

    @SuppressWarnings("unchecked")
    protected REQ getRequestByPaymentId(String paymentId) {
        PaymentOrderEntity order = orderService.selectByPk(paymentId);
        if (order == null) {
            return null;
        }
        return order.getOriginalRequest(getRequestType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<C> getConfigType() {
        if (configType == null) {
            configType = (Class<C>) ClassUtils.getGenericType(this.getClass(), 0);
        }
        return configType;
    }

    @Override
    public Class<REQ> getRequestType() {
        return (Class<REQ>) ClassUtils.getGenericType(this.getClass(), 1);
    }

    @Override
    public Class<RES> getResponseType() {
        return (Class<RES>) ClassUtils.getGenericType(this.getClass(), 2);
    }

    @Override
    public PaymentChannelConfigurator<C> getConfigurator() {
        return configurator;
    }

    @Override
    public void setConfigurator(PaymentChannelConfigurator<C> configurator) {
        this.configurator = configurator;
    }

}
