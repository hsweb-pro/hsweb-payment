package org.hswebframework.payment.payment.channel.manual;

import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.ChannelConfig;
import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.payment.withdraw.WithdrawPaymentChannel;
import org.hswebframework.payment.api.payment.withdraw.WithdrawPaymentRequest;
import org.hswebframework.payment.api.payment.withdraw.WithdrawPaymentResponse;
import org.hswebframework.payment.payment.channel.AbstractPaymentChannel;
import org.hswebframework.payment.payment.entity.PaymentOrderEntity;
import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 手动提现渠道
 *
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@Slf4j(topic = "system.payment.withdraw.manual")
public class OfflineWithdrawChannel extends AbstractPaymentChannel<ChannelConfig, WithdrawPaymentRequest<Payee>, WithdrawPaymentResponse>
        implements WithdrawPaymentChannel<WithdrawPaymentResponse, Payee> {

    @PostMapping("/withdraw/offline/{type}/{paymentId}")
    @Authorize(permission = "withdraw", action = Permission.ACTION_UPDATE)
    public ResponseMessage<Void> handleComplete(@PathVariable String paymentId,
                                                @PathVariable String type,
                                                @RequestBody String memo) {
        MDC.put("businessId", paymentId);
        PaymentOrderEntity orderEntity = orderService.selectByPk(paymentId);
        if (orderEntity == null) {
            throw new NotFoundException("支付订单不存在");
        }
        if (orderEntity.getTransType() != TransType.WITHDRAW) {
            throw new AccessDenyException("不能完成非提现订单");
        }
        if (!getChannelProvider().equals(orderEntity.getChannelProvider())
                || !getChannel().equals(orderEntity.getChannel())) {
            throw new AccessDenyException("不能完成非[" + getChannelName() + "]的提现申请");
        }
        log.info("手动提现交易完成:paymentId={},type={}", paymentId, type);
        afterHandleChannelNotify(ChannelNotificationResult.builder()
                .amount(orderEntity.getAmount())
                .memo(memo)
                .success("complete".equals(type) || "success".equals(type))
                .paymentId(paymentId)
                .build());
        return ResponseMessage.ok();
    }

    @Override
    public TransType getTransType() {
        return WithdrawPaymentChannel.super.getTransType();
    }

    @Override
    protected WithdrawPaymentResponse doRequestPay(ChannelConfig config, WithdrawPaymentRequest request) {
        log.info("发起手动提现交易:paymentId={}", request.getPaymentId());
        WithdrawPaymentResponse response = new WithdrawPaymentResponse();
        response.setSuccess(true);
        return response;
    }

    @Override
    public PayeeType getPayeeType() {
        return null;
    }

    @Override
    public boolean match(WithdrawPaymentRequest<Payee> request) {
        return true;
    }

    @Override
    public String getChannel() {
        return "offline-withdraw";
    }

    @Override
    public String getChannelName() {
        return "线下提现";
    }

    @Override
    public String getChannelProvider() {
        return "default-offline-withdraw";
    }

    @Override
    public String getChannelProviderName() {
        return "默认线下提现";
    }
}
