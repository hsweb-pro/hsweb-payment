package org.hswebframework.payment.merchant.entity;

import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.payment.payee.Payee;
import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mer_payee_info")
public class MerchantPayeeEntity extends SimpleGenericEntity<String> {
    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "is_default_withdraw")
    private Boolean defaultWithdraw;

    @Column(name = "payee")
    private String payee;

    @Column(name = "payee_name")
    private String payeeName;

    @Column(name = "payee_info_json")
    private String payeeInfoJson;

    @Column(name = "payee_type")
    private PayeeType payeeType;

    @Column(name = "comment")
    private String comment;

    @Column(name = "create_time")
    private Date createTime;

    @Valid
    @NotNull(message = "收款人不能为空")
    @SuppressWarnings("unchecked")
    public <P extends Payee> P getPayeeInfo() {
        if (StringUtils.isEmpty(payeeInfoJson)) {
            return null;
        }
        P payee = (P) JSON.parseObject(payeeInfoJson, payeeType.getPayeeType());
        if (StringUtils.isEmpty(payee.getPayee())) {
            payee.setPayee(getPayee());
        }
        if (StringUtils.isEmpty(payee.getPayeeName())) {
            payee.setPayeeName(getPayeeName());
        }
        return payee;
    }
}
