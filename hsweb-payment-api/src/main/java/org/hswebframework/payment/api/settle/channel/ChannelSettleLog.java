package org.hswebframework.payment.api.settle.channel;

import org.hswebframework.payment.api.enums.FundDirection;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 渠道结算日志
 */
@Getter
@Setter
public class ChannelSettleLog {

    private String id;

    private String accountNo;

    private String paymentId;

    private String merchantId;

    private String channelName;

    private String channelProvider;

    private long amount;

    private FundDirection fundDirection;

    private String memo;

    private Date createTime;


}
