package org.hswebframework.payment.payment.controller;

import org.hswebframework.payment.api.payment.ActiveQuerySupportPaymentChannel;
import org.hswebframework.payment.api.payment.PaymentChannel;
import org.hswebframework.payment.api.payment.PaymentService;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.api.payment.order.PaymentOrderService;
import org.hswebframework.payment.payment.controller.response.ChannelConfigProperty;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@Authorize(permission = "payment")
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentOrderService paymentOrderService;

    @GetMapping("/channels")
    @Authorize(permission = Permission.ACTION_GET)
    @ApiOperation("获取所有支持的支付渠道")
    public ResponseMessage<List<PaymentChannelInfo>> getAllPaymentChannel() {
        return ResponseMessage.ok(paymentService
                .getAllChannel()
                .stream()
                .sorted(Comparator.comparingInt(channel -> channel.getTransType().ordinal()))
                .map(PaymentChannelInfo::of)
                .collect(Collectors.toList()));
    }

    @PostMapping("/active-query/{paymentId}")
    @Authorize(permission = Permission.ACTION_UPDATE)
    @ApiOperation("主动发起查询")
    public ResponseMessage<List<PaymentChannelInfo>> doActiveQuery(@PathVariable String paymentId) {
        PaymentOrder order = paymentOrderService.getOrderById(paymentId);
        if (order == null) {
            throw new NotFoundException("订单不存在");
        }
        MDC.put("businessId",paymentId);

        ActiveQuerySupportPaymentChannel paymentChannel = paymentService.getAllChannel()
                .stream()
                .filter(ActiveQuerySupportPaymentChannel.class::isInstance)
                .map(ActiveQuerySupportPaymentChannel.class::cast)
                .filter(channel ->
                        channel.getChannelProvider().equals(order.getChannelProvider())
                                && channel.getTransType() == order.getTransType()
                                && channel.getChannel().equals(order.getChannel()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("此订单不支持主动查询"));
        paymentChannel.doActiveQueryOrderResult(order);

        return ResponseMessage.ok();
    }

    @Getter
    @Setter
    public static class PaymentChannelInfo {
        private String transType;

        private String transTypeName;

        private String channelProvider;

        private String channelProviderName;

        private String channel;

        private String channelName;

        private List<ChannelConfigProperty> properties = new ArrayList<>();

        public static PaymentChannelInfo of(PaymentChannel channel) {
            PaymentChannelInfo channelInfo = new PaymentChannelInfo();
            channelInfo.setChannel(channel.getChannel());
            channelInfo.setChannelName(channel.getChannelName());
            channelInfo.setTransTypeName(channel.getTransType().getText());
            channelInfo.setChannelProvider(channel.getChannelProvider());
            channelInfo.setChannelProviderName(channel.getChannelProviderName());
            channelInfo.setTransType(channel.getTransType().getValue());
            Class<?> configType = channel.getConfigType();
            ReflectionUtils.doWithFields(configType, field -> {
                ApiModelProperty swaggerAnnotation = field.getAnnotation(ApiModelProperty.class);
                if (swaggerAnnotation == null) {
                    return;
                }
                ChannelConfigProperty property = new ChannelConfigProperty();
                property.initFromSwaggerAnnotation(field, swaggerAnnotation);
                channelInfo.properties.add(property);
            });
            return channelInfo;
        }
    }
}
