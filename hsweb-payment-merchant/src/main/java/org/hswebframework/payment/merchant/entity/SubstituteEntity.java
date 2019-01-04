package org.hswebframework.payment.merchant.entity;

import org.hswebframework.payment.api.enums.SubstituteStatus;
import org.hswebframework.payment.api.enums.PayeeType;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * 代付申请信息
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Table(name = "mer_substitute")
public class SubstituteEntity extends SimpleGenericEntity<String> {

    @Column(name = "merchant_id", updatable = false)
    private String merchantId;

    @Column(name = "trans_no", updatable = false)
    private String transNo;

    @Column(name = "merchant_name", updatable = false)
    private String merchantName;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "real_amount")
    private Long realAmount;

    @Column(name = "charge")
    private Long charge;

    @Column(name = "real_charge")
    private Long realCharge;

    @Column(name = "total")
    private Integer total;

    @Column(name = "total_success")
    private Integer totalSuccess;

    @Column(name = "payment_id", updatable = false)
    private String paymentId;

    @Column(name = "status")
    private SubstituteStatus status;

    //收款人类型,比如 银行卡，支付宝
    @Column(name = "payee_type", updatable = false)
    private PayeeType payeeType;

    @Column(name = "complete_time")
    private Date completeTime;

    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @Column(name = "notify_url")
    private String notifyUrl;

    @Column(name = "remark")
    private String remark;

}
