package org.hswebframework.payment.api.settle.channel;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 渠道结算信息
 */
@Getter
@Setter
public class ChannelSettleInfo {

    private String id;

    private String accountNo;

    private String name;

    private Date createTime;

    private Date updateTime;

    //结算余额
    private long balance;

    private String comment;

}
