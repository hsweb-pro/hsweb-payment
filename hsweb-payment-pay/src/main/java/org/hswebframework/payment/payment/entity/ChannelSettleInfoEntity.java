package org.hswebframework.payment.payment.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * 渠道结算信息
 */
@Getter
@Setter
@Table(name = "pay_channel_settle")
public class ChannelSettleInfoEntity extends SimpleGenericEntity<String> {

    //结算账号
    @Column(name = "account_no")
    private String accountNo;

    //名称
    @Column(name = "name")
    private String name;

    //创建时间
    @Column(name = "create_time")
    private Date createTime;

    //结算余额
    @Column(name = "balance")
    private Long balance;

    //说明
    @Column(name = "comment")
    private String comment;

}
