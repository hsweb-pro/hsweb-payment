package org.hswebframework.payment.payment.channel;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.exception.BusinessException;
import org.hswebframework.payment.api.payment.*;
import org.hswebframework.payment.payment.service.LocalChannelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Component
@Slf4j
public class SmartPaymentChannelSelector implements PaymentChannelSelector {

    @Autowired
    private LocalChannelConfigService channelConfigService;

    private PaymentChannelSelector selector = RandomPaymentChannelSelector.INSTANCE;

    @Override
    public <REQ extends PaymentRequest, RES extends PaymentResponse> PaymentChannel<REQ, RES> select(PaymentRequest request,
                                                                                                     TransType transType,
                                                                                                     List<PaymentChannel<REQ, RES>> allChannel) {
        AtomicReference<BusinessException> errReason = new AtomicReference<>();
        Map<PaymentChannel, String> configIdCache = new HashMap<>();

        List<PaymentChannel<REQ, RES>> channelList = allChannel.stream()
                .filter(channel -> !channelConfigService
                        .queryByTransTypeAndChannel(channel.getTransType(), channel.getChannel(), channel.getChannelProvider())
                        .isEmpty())
                .filter(channel -> {
                    if (channel instanceof ConfigurablePaymentChannel) {
                        ChannelConfig config;
                        if (StringUtils.hasText(request.getChannelId())) {
                            config = ((ConfigurablePaymentChannel) channel).getConfigurator()
                                    .getPaymentConfigById(request.getChannelId());
                        } else {
                            //获取一个渠道配置,如果渠道配置不存在,则认为渠道不可用
                            try {
                                config = ((ConfigurablePaymentChannel) channel).getConfigurator()
                                        .getPaymentConfigByMerchantId(request.getMerchantId(), transType, request.getAmount());
                                if (null != config) {
                                    configIdCache.put(channel, config.getId());
                                }
                            } catch (BusinessException e) {
                                errReason.set(e);
                                return false;
                            }
                        }
                        return config != null;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        if (channelList.isEmpty()) {
            if (errReason.get() != null) {
                throw errReason.get();
            }
            throw ErrorCode.CHANNEL_CONFIG_ERROR.createException("没有可用的渠道配置");
        }
        PaymentChannel<REQ, RES> channel = selector.select(request, transType, channelList);
        if (channel != null) {//绑定渠道配置ID
            request.setChannelId(configIdCache.get(channel));
        }
        return channel;
    }
}
