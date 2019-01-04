package org.hswebframework.payment.merchant.entity;

import org.hswebframework.payment.api.enums.TransRateType;
import org.hswebframework.payment.api.enums.TransType;
import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Table
public class MerchantChargeEntity extends SimpleGenericEntity<String> {

    /**
     * 支付订单号
     *
     * @see org.hswebframework.payment.api.payment.order.PaymentOrder#id
     */
    @Column(name = "payment_id")
    private String paymentId;

    /**
     * 商户ID
     */
    @Column(name = "merchant_id")
    private String merchantId;

    /**
     * 交易类型
     */
    @Column(name = "tans_type")
    private TransType transType;

    /**
     * 是否已结算
     */
    @Column(name = "is_settle")
    private Boolean settle;

    /**
     * 结算时间
     */
    @Column(name = "settle_time")
    private Date settleTime;

    /**
     * 支付时间
     */
    @Column(name = "pay_time")
    private Date payTime;

    /**
     * 订单金额,单位:分
     */
    @Column(name = "amount")
    private Long amount;

    /**
     * 已计算收费
     */
    @Column(name = "is_calculated")
    private Boolean chargeCalculated;

    /**
     * 平台收费,单位:分
     */
    @Column(name = "charge")
    private Long charge;

    //渠道收费
    @Column(name = "channel_charge")
    private Long channelCharge;

    /**
     * 代理商收费,单位:分
     */
    @Column(name = "agent_charge")
    private Long agentCharge;

    /**
     * 收费说明
     */
    @Column(name = "charge_memo")
    private String chargeMemo;

    /**
     * 代理收费说明
     */
    @Column(name = "agent_charge_memo")
    private String agentChargeMemo;

    @Column(name = "channel_charge_memo")
    private String channelChargeMemo;

    /**
     * 备注
     */
    @Column(name = "memo")
    private String memo;

    public void chargeComplete(TransRateType.TransCharge charge) {
        setChargeCalculated(true);
        setCharge(charge.getCharge());
        setChargeMemo(charge.toString());
    }

    public void agentChargeComplete(TransRateType.TransCharge charge) {
        setChargeCalculated(true);
        setAgentCharge(charge.getCharge());
        setAgentChargeMemo(charge.toString());
    }

    public void channelChargeComplete(TransRateType.TransCharge charge) {
        setChargeCalculated(true);
        setChannelCharge(charge.getCharge());
        setChannelChargeMemo(charge.toString());
    }
    /**
     * 获取收费总计.
     *
     * @return 收费总计
     */
    public long getTotalCharge() {
        return agentCharge > 0 ? agentCharge : charge;
    }

    /**
     * 代理实际收费 = 代理费率-平台费率
     *
     * @return 代理实际收费
     */
    public long getAgentRealCharge() {
        return Math.max(agentCharge - charge, 0);
    }
}
