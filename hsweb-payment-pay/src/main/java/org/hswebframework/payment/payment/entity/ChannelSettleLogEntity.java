package org.hswebframework.payment.payment.entity;

import org.hswebframework.payment.api.enums.FundDirection;
import org.hswebframework.payment.api.enums.TransType;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * 渠道结算日志
 */
@Getter
@Setter
@Table(name = "pay_channel_settle_log")
public class ChannelSettleLogEntity extends SimpleGenericEntity<String> {

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "trans_type")
    private TransType transType;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "channel")
    private String channel;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "channel_provider")
    private String channelProvider;

    @Column(name = "channel_provide_name")
    private String channelProviderName;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "balance")
    private Long balance;

    @Column(name = "fund_direction")
    private FundDirection fundDirection;

    @Column(name = "memo")
    private String memo;

    @Column(name = "create_time")
    private Date createTime;


}
