package org.hswebframework.payment.api.payment;

import org.hswebframework.payment.api.enums.TimeUnit;
import org.hswebframework.payment.api.enums.TransRateType;
import org.hswebframework.payment.api.selector.SelectorOption;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.bean.ToString;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ChannelConfig extends SelectorOption {

    private String name;

    //渠道结算账户
    private String accountNo;

    //费率计算统计周期单位
    private TimeUnit chargeTimeUnit = TimeUnit.SINGLE;

    //费率计算周期
    private int chargeTime = 1;

    //费率类型
    private TransRateType rateType;

    private String rate;

    private String channel;

    private String channelProvider;

    @ApiModelProperty("渠道通知根地址")
    private String notifyLocation;

    @ApiModelProperty("服务器根地址")
    private String serverLocation;

    @ApiModelProperty("每秒最大交易量")
    private double maximumTradingPerSecond = -1D;

    @ApiModelProperty("固定订单描述")
    private String orderComment;

    /**
     * 交易限额,对整个渠道的交易限额
     */
    private List<TradingLimit> tradingLimits;

    @Override
    public String toString() {
        return ToString.toString(this);
    }

    public long calculateRate(long amount) {
        if (rateType == null || StringUtils.isEmpty(rate)) {
            return 0;
        }
        return rateType.calculate(amount, rate).getCharge();
    }

    public TransRateType.TransCharge calculateCharge(long amount) {
        if (rateType == null || StringUtils.isEmpty(rate)) {
            return TransRateType.TransCharge.none;
        }
        return rateType.calculate(amount, rate);
    }
}
