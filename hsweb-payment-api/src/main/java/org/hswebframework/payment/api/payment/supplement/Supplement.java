package org.hswebframework.payment.api.payment.supplement;

import org.hswebframework.payment.api.enums.SupplementStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Supplement {

    private String id;

    private String sourceChannelId;

    //补登目标渠道
    private String targetChannelId;

    //补登金额
    private Long amount;

    //创建时间
    private Date createTime;

    //补等时间
    private Date supplementTime;

    //创建人ID
    private String creatorId;

    //创建人姓名
    private String creatorName;

    //状态
    private SupplementStatus status;

    //备注
    private String remark;
}