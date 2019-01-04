package org.hswebframework.payment.payment.settle;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.enums.TransRateType;
import org.hswebframework.payment.api.enums.TransRateType.TransCharge;
import org.hswebframework.payment.api.payment.ChannelConfig;
import org.hswebframework.payment.api.payment.ChannelConfigManager;
import org.hswebframework.payment.api.payment.events.GatewayPaymentCompleteEvent;
import org.hswebframework.payment.api.payment.events.QuickPaymentCompleteEvent;
import org.hswebframework.payment.api.payment.events.SubstitutePaymentCompleteEvent;
import org.hswebframework.payment.api.payment.events.WithdrawPaymentCompleteEvent;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.api.settle.channel.ChannelDepositRequest;
import org.hswebframework.payment.api.settle.channel.ChannelSettleService;
import org.hswebframework.payment.api.settle.channel.ChannelWithdrawRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 渠道结算服务,在交易完成之后,对渠道资金账户进行相关处理
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ChannelSettleTransService {

    @Autowired
    private ChannelConfigManager channelConfigManager;

    @Autowired
    private ChannelSettleService channelSettleService;

    private void doChannelDeposit(PaymentOrder order) {
        ChannelConfig channelConfig = channelConfigManager.getChannelConfigById(order.getChannelId(), ChannelConfig.class)
                .orElseThrow(() -> ErrorCode.CHANNEL_CONFIG_ERROR.createException("渠道配置[" + order.getChannelId() + "]不存在"));

        long amount = order.getRealAmount();
        TransCharge charge = channelConfig.calculateCharge(amount);
        long realAmount = amount - charge.getCharge();

        if (realAmount == 0) {
            //0收入不上帐
            return;
        }

        ChannelDepositRequest request = ChannelDepositRequest.builder()
                .amount(realAmount)//交易金额-渠道费率
                .channelId(order.getChannelId())
                .paymentId(order.getId())
                .channel(order.getChannel())
                .merchantId(order.getMerchantId())
                .merchantName(order.getMerchantName())
                .transType(order.getTransType())
                .memo("订单交易额:" + TransCharge.format(amount) +
                        "元;渠道收费:" + charge.toString())
                .build();

        channelSettleService.deposit(request).assertSuccess();
    }

    private void doChannelWithdraw(PaymentOrder order) {
        ChannelConfig channelConfig = channelConfigManager.getChannelConfigById(order.getChannelId(), ChannelConfig.class)
                .orElseThrow(() -> ErrorCode.CHANNEL_CONFIG_ERROR.createException("渠道配置[" + order.getChannelId() + "]不存在"));

        long amount = order.getRealAmount();
        TransCharge charge = channelConfig.calculateCharge(amount);
        long realAmount = amount - charge.getCharge();

        if (realAmount == 0) {
            //0支出不下帐
            return;
        }
        ChannelWithdrawRequest request = ChannelWithdrawRequest.builder()
                .amount(amount + charge.getCharge())//交易金额+渠道费率
                .channelId(order.getChannelId())
                .paymentId(order.getId())
                .channel(order.getChannel())
                .merchantId(order.getMerchantId())
                .merchantName(order.getMerchantName())
                .transType(order.getTransType())
                .memo(charge.getCharge() > 0 ? "手续费:" + channelConfig.getRateType().getDescription(channelConfig.getRate()) : "")
                .build();

        channelSettleService.withdraw(request).assertSuccess();
    }

    @EventListener
    public void handleGatewayPaymentService(GatewayPaymentCompleteEvent completeEvent) {
        PaymentOrder order = completeEvent.getOrder();
        if (order.getStatus() == PaymentStatus.success) {
            //网关支付交易成功,执行渠道结算上账
            doChannelDeposit(order);
        }
    }

    @EventListener
    public void handleQuickPaymentService(QuickPaymentCompleteEvent completeEvent) {
        PaymentOrder order = completeEvent.getOrder();
        if (order.getStatus() == PaymentStatus.success) {
            //快捷支付交易成功,执行渠道结算上账
            doChannelDeposit(order);
        }
    }

    //处理提现
    @EventListener
    public void handleWithDrawPaymentService(WithdrawPaymentCompleteEvent completeEvent) {
        PaymentOrder order = completeEvent.getOrder();
        if (order.getStatus() == PaymentStatus.success) {
            //提现交易成功,执行渠道结算下账
            doChannelWithdraw(order);
        }
    }

}
