package org.hswebframework.payment.payment.entity;

import org.hswebframework.payment.api.enums.SupplementStatus;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * 渠道结算交易补登记录
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Table(name = "pay_settle_supplement")
@Getter
@Setter
public class SupplementEntity extends SimpleGenericEntity<String> {

    //借方账户
    @Column(name = "source_account_no")
    private String sourceAccountNo;

    @Column(name = "source_account_name")
    private String sourceAccountName;

    //借方金额
    @Column(name = "source_amount", updatable = false)
    private Long sourceAmount;

    //贷方账户
    @Column(name = "target_account_no", nullable = false)
    private String targetAccountNo;

    @Column(name = "target_account_name")
    private String targetAccountName;
    //贷方金额
    @Column(name = "target_amount", updatable = false)
    private Long targetAmount;

    //创建时间
    @Column(name = "create_time", nullable = false)
    private Date createTime;

    //补等时间
    @Column(name = "supplement_time", nullable = false)
    private Date supplementTime;

    //创建人ID
    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    //创建人姓名
    @Column(name = "creator_name", nullable = false)
    private String creatorName;

    //状态
    @Column(name = "status", nullable = false)
    private SupplementStatus status;

    //备注
    @Column(name = "remark", nullable = false)
    private String remark;

}
