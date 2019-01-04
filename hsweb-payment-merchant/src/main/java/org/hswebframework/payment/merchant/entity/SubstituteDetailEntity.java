package org.hswebframework.payment.merchant.entity;

import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.api.enums.SubstituteDetailStatus;
import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.payment.payee.Payee;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 代付详情信息
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Table(name = "mer_substitute_detail")
public class SubstituteDetailEntity extends SimpleGenericEntity<String> {

    @Column(name = "substitute_id", updatable = false)
    private String substituteId;

    @Column(name = "merchant_id", updatable = false)
    private String merchantId;

    @Column(name = "merchant_name", updatable = false)
    private String merchantName;

    @Column(name = "payment_id", updatable = false)
    private String paymentId;

    @Column(name = "tans_no", updatable = false)
    private String transNo;

    @Column(name = "payee", updatable = false)
    private String payee;

    @Column(name = "payee_name", updatable = false)
    private String payeeName;

    @Column(name = "amount", updatable = false)
    private Long amount;

    @Column(name = "status", updatable = false)
    private SubstituteDetailStatus status;

    @Column(name = "payee_info_json", updatable = false)
    private String payeeInfoJson;

    @Column(name = "remark")
    private String remark;

    @Column(name = "charge_amount")
    private Long chargeAmount;

    @Column(name = "charge_memo")
    private String chargeMemo;


    public <T extends Payee> T getPayeeInfo(PayeeType target) {
        if (StringUtils.isEmpty(payeeInfoJson)) {
            return null;
        }
        return (T) JSON.parseObject(payeeInfoJson, target.getPayeeType());
    }
}
