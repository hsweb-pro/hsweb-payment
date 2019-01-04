package org.hswebframework.payment.payment.service.impl;

import org.hswebframework.payment.api.concurrent.DuplicateValidatorManager;
import org.hswebframework.payment.api.enums.BindCardPurpose;
import org.hswebframework.payment.api.enums.BindCardStatus;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.exception.BusinessException;
import org.hswebframework.payment.api.payment.*;
import org.hswebframework.payment.api.payment.bind.*;
import org.hswebframework.payment.api.payment.bind.channel.*;
import org.hswebframework.payment.api.payment.gateway.GateWayPaymentService;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.payment.quick.*;
import org.hswebframework.payment.api.payment.quick.channel.ChannelQuickPaymentConfirmRequest;
import org.hswebframework.payment.api.payment.quick.channel.ChannelQuickPaymentConfirmResponse;
import org.hswebframework.payment.api.payment.quick.channel.ChannelQuickPaymentRequest;
import org.hswebframework.payment.api.payment.quick.channel.QuickPaymentChannel;
import org.hswebframework.payment.api.payment.substitute.SubstitutePaymentService;
import org.hswebframework.payment.api.payment.substitute.request.SubstituteRequest;
import org.hswebframework.payment.api.payment.substitute.response.SubstituteResponse;
import org.hswebframework.payment.api.payment.withdraw.WithdrawPaymentRequest;
import org.hswebframework.payment.api.payment.withdraw.WithdrawPaymentResponse;
import org.hswebframework.payment.api.payment.withdraw.WithdrawPaymentService;
import org.hswebframework.payment.payment.entity.BindCardEntity;
import org.hswebframework.payment.payment.entity.PaymentOrderEntity;
import org.hswebframework.payment.payment.events.BindCardRequestSuccessEvent;
import org.hswebframework.payment.payment.events.PaymentRequestAfterEvent;
import org.hswebframework.payment.payment.events.PaymentRequestBeforeEvent;
import org.hswebframework.payment.payment.exception.PaymentException;
import org.hswebframework.payment.payment.service.LocalBindCardService;
import org.hswebframework.payment.payment.service.LocalPaymentOrderService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.bean.BeanValidator;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.validate.ValidationException;
import org.joda.time.DateTime;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
@Slf4j
@SuppressWarnings("unchecked")
//@Transactional(rollbackFor = Throwable.class)
public class DefaultPaymentService implements PaymentService,
        GateWayPaymentService,
        QuickPaymentService,
        BeanPostProcessor,
        WithdrawPaymentService,
        BindCardService, SubstitutePaymentService {

    @Getter
    @Setter
    @Value("${hsweb.server.location:http://localhost:8080/}")
    private String serverLocation;

    //Map<交易类型,Map<渠道标识,List<渠道>>
    private Map<TransType, Map<String, List<PaymentChannel>>> allChannel = new ConcurrentHashMap<>();

    private Map<BindCardPurpose, Map<String, BindCardChannel>> bindCardChannel = new ConcurrentHashMap<>();

    @Override
    public List<PaymentChannel> getAllChannel() {
        return allChannel.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Autowired
    private LocalBindCardService bindCardService;

    @Autowired
    private LocalPaymentOrderService localPaymentOrderService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private DuplicateValidatorManager validatorManager;

    @Autowired(required = false)
    private PaymentChannelSelector paymentChannelSelector = RandomPaymentChannelSelector.INSTANCE;

    @Override
    public GateWayPaymentService gateway() {
        return this;
    }

    @Override
    public QuickPaymentService quick() {
        return this;
    }

    @Override
    public WithdrawPaymentService withdraw() {
        return this;
    }

    @Override
    public SubstitutePaymentService substitute() {
        return this;
    }

    @Override
    public PaymentResponse requestGateWayPayment(PaymentRequest request) throws PaymentException, ValidationException {
        return doRequestPay(TransType.GATEWAY, request);
    }

    protected void tryValidateDuplicate(PaymentRequest request) {
//        validatorManager.getValidator("payment-order")
//                .tryPut(request.getOrderId(), "重复的支付请求");
    }

    protected <REQ extends PaymentRequest, RES extends PaymentResponse> RES doRequestPay(TransType transType, REQ request) {
        //验证是否为重复订单
        tryValidateDuplicate(request);
        if (StringUtils.isEmpty(request.getPaymentId())) {
            request.setPaymentId(IDGenerator.SNOW_FLAKE_STRING.generate());
        }
        String oldBusinessId = MDC.get("businessId");
        MDC.put("businessId", request.getPaymentId());
        BeanValidator.tryValidate(request);
        //获取渠道
        PaymentChannel<REQ, RES> channel = getChannel(request, transType, request.getChannel());
        publisher.publishEvent(new PaymentRequestBeforeEvent(request.getPaymentId(), transType, channel, request));
        //发起支付
        RES response = channel.requestPayment(request);
        response.setPaymentId(request.getPaymentId());
        response.setRequestId(request.getRequestId());
        //推送事件
        publisher.publishEvent(new PaymentRequestAfterEvent(request.getPaymentId(), transType, request, response));
        MDC.put("businessId", oldBusinessId);
        return response;
    }

    @Override
    public <P extends Payee> SubstituteResponse
    requestSubstitute(SubstituteRequest<P> request) {
        return doRequestPay(TransType.SUBSTITUTE, request);
    }

    @Override
    public QuickPaymentResponse requestQuickPayment(QuickPaymentRequest request) {

        ChannelQuickPaymentRequest channelRequest = FastBeanCopier.copy(request, new ChannelQuickPaymentRequest());
        if (StringUtils.hasText(request.getBindId())) {
            BindCardEntity bindCard = bindCardService.selectByPk(request.getBindId());
            if (null == bindCard) {
                throw ErrorCode.BIND_CARD_NOT_EXISTS.createException();
            }
            FastBeanCopier.copy(request, channelRequest);
            channelRequest.setBindId(bindCard.getId());
            channelRequest.setChannelAuthorizeCode(bindCard.getAuthorizeCode());
        }
        PaymentResponse channelResponse = doRequestPay(TransType.QUICK, channelRequest);

        return FastBeanCopier.copy(channelResponse, QuickPaymentResponse::new);
    }

    @Override
    public QuickPaymentConfirmResponse confirmQuickPayment(QuickPaymentConfirmRequest request) {
        ChannelQuickPaymentConfirmRequest confirmRequest = FastBeanCopier.copy(request, ChannelQuickPaymentConfirmRequest::new);

        //获取订单
        PaymentOrderEntity order = localPaymentOrderService.selectByPk(request.getPaymentId());
//        //获取快捷支付申请的原始请求
        ChannelQuickPaymentRequest originalRequest = order.getOriginalRequest(ChannelQuickPaymentRequest.class);
//        //获取绑卡信息
//        String bindId = originalRequest.getBindId();
//        BindCardEntity bindCardEntity = bindCardService.selectByPk(bindId);
//        confirmRequest.setBindCard(bindCardEntity.copyTo(new BindCard()));
        //发起确认
        ChannelQuickPaymentConfirmResponse confirmResponse =
                ((QuickPaymentChannel) getChannel(originalRequest, TransType.QUICK, order.getChannel()))
                        .confirmQuickPay(confirmRequest);
        return FastBeanCopier.copy(confirmResponse, QuickPaymentConfirmResponse::new);
    }

    protected void registerChannel(PaymentChannel channel) {
        log.debug("register pay channel:{}", channel);
        getChannel(channel.getTransType())
                .computeIfAbsent(channel.getChannel(), c -> new ArrayList<>())
                .add(channel);
    }

    private <REQ extends PaymentRequest, RES extends PaymentResponse> Map<String, List<PaymentChannel<REQ, RES>>> getChannel(TransType method) {
        return (Map) allChannel.computeIfAbsent(method, m -> new HashMap<>());
    }

    private <REQ extends PaymentRequest,
            RES extends PaymentResponse> PaymentChannel<REQ, RES> getChannel(REQ request, TransType transType, String channel) {
        Map<String, List<PaymentChannel<REQ, RES>>> channels = getChannel(transType);
        List<PaymentChannel<REQ, RES>> channelList = channels.get(channel)
                .stream()
                .filter(c -> c.match(request))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(channelList)) {
            throw ErrorCode.CHANNEL_UNSUPPORTED.createException();
        }
        //选择一个渠道服务
        return paymentChannelSelector.select(request, transType, channelList);
    }


    private Map<String, BindCardChannel> getBindCardChannel(BindCardPurpose purpose) {
        return this.bindCardChannel.computeIfAbsent(purpose, k -> new HashMap<>());
    }

    void registerChannel(BindCardChannel bindCardChannel) {
        log.debug("register bind card channel:{}", bindCardChannel);
        getBindCardChannel(bindCardChannel.getPurpose())
                .put(bindCardChannel.getChannel(), bindCardChannel);
    }


    @Override
    public WithdrawPaymentResponse requestWithdrawPayment(WithdrawPaymentRequest request) {
        return doRequestPay(TransType.WITHDRAW, request);
    }

    public BindCardChannel getBindCardChannel(BindCardPurpose purpose, String channel) {
        BindCardChannel cardChannel = getBindCardChannel(purpose).get(channel);
        if (null == cardChannel) {
            throw ErrorCode.CHANNEL_UNSUPPORTED.createException();
        }
        return cardChannel;
    }

    @Override
    public BindCardResponse requestBindCard(BindCardRequest request) {
        BeanValidator.tryValidate(request);
        //获取渠道
        BindCardChannel cardChannel = getBindCardChannel(request.getPurpose(), request.getChannel());
        //构造渠道请求
        ChannelBindCardRequest channelBindCardRequest = FastBeanCopier.copy(request, ChannelBindCardRequest::new);
        //发起请求并断言成功
        ChannelBindCardResponse response = cardChannel.requestBindCard(channelBindCardRequest);
        response.assertSuccess();//如果失败直接抛出异常

        //构造响应
        BindCardResponse bindCardResponse = FastBeanCopier.copy(response, BindCardResponse::new);

        //插入绑卡记录
        BindCardEntity bindCardEntity = FastBeanCopier.copy(request, bindCardService::createEntity);
        bindCardEntity.requestComplete(response.getBindConfirmCode());
        bindCardEntity.setChannelName(cardChannel.getChannelName());
        bindCardEntity.setChannel(cardChannel.getChannel());
        bindCardEntity.setChannelId(response.getChannelId());

        String id = bindCardService.insert(bindCardEntity);
        bindCardResponse.setBindId(id);
        //推送绑卡申请成功事件
        publisher.publishEvent(new BindCardRequestSuccessEvent(request, bindCardResponse));
        return bindCardResponse;
    }

    @Override
    public BindCardConfirmResponse confirmBindCard(BindCardConfirmRequest request) {
        BeanValidator.tryValidate(request);
        //拿到帮卡申请
        String bindId = request.getBindId();
        BindCardEntity entity = bindCardService.selectByPk(bindId);
        if (entity == null) {
            throw ErrorCode.MERCHANT_NOT_EXISTS.createException();
        }
        if (entity.getStatus() != BindCardStatus.binding) {
            throw ErrorCode.BIND_CARD_ALREADY_BIND.createException();
        }
        BindCard bindingCard = entity.copyTo(new BindCard());
        //调用渠道进行确认
        BindCardChannel cardChannel = getBindCardChannel(bindingCard.getPurpose(), bindingCard.getChannel());
        ChannelConfirmRequest channelConfirmRequest = FastBeanCopier.copy(request, ChannelConfirmRequest::new);
        channelConfirmRequest.setBindingCard(bindingCard);
        ChannelConfirmResponse channelResponse = cardChannel.confirmBindCard(channelConfirmRequest);
        //完成绑卡
        entity.confirmComplete(channelResponse.isSuccess(), channelResponse.getAuthorizeCode());
        bindCardService.updateByPk(bindId, entity);
        entity.copyTo(bindingCard);
        BindCardConfirmResponse response = FastBeanCopier.copy(channelResponse, BindCardConfirmResponse::new);
        response.setBindCard(bindingCard);
        return response;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private <REQ extends PaymentRequest,
            RES extends PaymentResponse> PaymentChannel<REQ, RES> getChannel(String provider, TransType transType, String channel, PaymentOrderEntity order) {
        Map<String, List<PaymentChannel<REQ, RES>>> channels = getChannel(transType);

        //选择一个配置
        PaymentChannel<REQ, RES> paymentChannel = channels.get(channel)
                .stream()
                .filter(c -> c.getChannelProvider().equals(provider))
                .filter(c -> c.match(order.getOriginalRequest(c.getRequestType())))
                .findFirst()
                .orElse(channels.size() > 0 ? channels.get(channel).get(0) : null);
        if (null == paymentChannel) {
            throw ErrorCode.CHANNEL_UNSUPPORTED.createException();
        }
        return paymentChannel;
    }

    @Scheduled(fixedRate = 10 * 60 * 1000, initialDelay = 10 * 1000)
    protected void doActiveQueryOrder() {
        List<String> timeoutIdList = new ArrayList<>();
        //10分钟之前
        Date time = new DateTime().plusMinutes(-10).toDate();
        localPaymentOrderService
                .queryPayingOrder(time, 50)
                .forEach(order -> {
                    try (MDC.MDCCloseable closeable = MDC.putCloseable("businessId", order.getId())) {
                        //20分钟还没有支付完成认为超时
                        if (System.currentTimeMillis() - order.getCreateTime().getTime() > TimeUnit.MINUTES.toMillis(20)) {
                            timeoutIdList.add(order.getId());
                            return;
                        }
                        PaymentChannel channel = getChannel(order.getChannelProvider(), order.getTransType(), order.getChannel(), order);
                        if (channel instanceof ActiveQuerySupportPaymentChannel) {
                            ActiveQuerySupportPaymentChannel queryChannel = ((ActiveQuerySupportPaymentChannel) channel);
                            queryChannel.doActiveQueryOrderResult(order.copyTo(new PaymentOrder()));
                        } else {
                            //不支持则认为超时
                            timeoutIdList.add(order.getId());
                        }
                    } catch (BusinessException e) {
                        if (ErrorCode.CHANNEL_UNSUPPORTED.getValue().equals(e.getCode())) {
                            //也认为超时
                            timeoutIdList.add(order.getId());
                        }
                    } catch (Exception e) {
                        log.error("主动发起渠道[{}-{}]订单[{}]查询失败",
                                order.getChannelProviderName(), order.getChannelName(), order.getId(), e);
                    }
                });
        if (!CollectionUtils.isEmpty(timeoutIdList)) {
            localPaymentOrderService.updateTimeoutStatus(timeoutIdList);
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof PaymentChannel) {
            registerChannel((PaymentChannel) bean);
        }
        if (bean instanceof BindCardChannel) {
            registerChannel(((BindCardChannel) bean));
        }
        return bean;
    }


}
