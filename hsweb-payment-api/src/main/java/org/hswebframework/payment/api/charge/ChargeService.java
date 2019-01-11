package org.hswebframework.payment.api.charge;

import org.hswebframework.payment.api.enums.TransRateType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.merchant.AgentMerchant;
import io.vavr.Function4;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 收费服务
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface ChargeService {

    void calculate(String merchantId,
                   TransType transType,
                   String channel,
                   long amount,
                   //是否计算代理
                   boolean calculateAgent,
                   // 平台的收费
                   Consumer<TransRateType.TransCharge> chargeConsumer,
                   // 对商户的收费
                   Consumer<TransRateType.TransCharge> merchantChargeConsumer,
                   // 多级代理收费回调,<代理商,代理收费,实际收费,上级收费,Void>
                   // 实际收费=代理收费-上级收费
                   Function4<AgentMerchant, TransRateType.TransCharge, Long, TransRateType.TransCharge, Void> agentChargeConsumer);
}
