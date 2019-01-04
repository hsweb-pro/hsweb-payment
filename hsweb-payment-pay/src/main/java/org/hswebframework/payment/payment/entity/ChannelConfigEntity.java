package org.hswebframework.payment.payment.entity;

import org.hswebframework.payment.api.enums.TransRateType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.TradingLimit;
import org.hswebframework.payment.api.payment.ChannelConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Table
public class ChannelConfigEntity extends SimpleGenericEntity<String> {

    @Column(name = "name")
    private String name;

    @Column(name = "trans_type")
    private TransType transType;

    @Column(name = "channel")
    private String channel;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "channel_provider")
    private String channelProvider;

    @Column(name = "channel_provider_name")
    private String channelProviderName;

    @Column(name = "status")
    private Byte status;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "rate_type")
    private TransRateType rateType;

    @Column(name = "rate")
    private String rate;

    /**
     * @see org.hswebframework.payment.api.payment.TradingLimit
     */
    @Column(name = "trading_limits_json")
    private List<TradingLimit> tradingLimits;

    @SneakyThrows
    public <T extends ChannelConfig> T toChannelConfig(Class<T> channelConfigClass) {
        T config = CollectionUtils.isEmpty(getProperties()) ? channelConfigClass.newInstance() : FastBeanCopier.copy(getProperties(), channelConfigClass);
        config.setId(getId());
        config.setChannel(channel);
        config.setChannelProvider(channelProvider);
        config.setAccountNo(accountNo);
        config.setRate(rate);
        config.setName(name);
        config.setRateType(rateType);
        config.setTradingLimits(getTradingLimits());
        return config;
    }

}
