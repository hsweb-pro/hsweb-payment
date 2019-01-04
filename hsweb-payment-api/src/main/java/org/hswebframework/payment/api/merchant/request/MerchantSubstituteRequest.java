package org.hswebframework.payment.api.merchant.request;

import org.hswebframework.payment.api.ApiRequest;
import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.payment.substitute.request.SubstituteDetail;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class MerchantSubstituteRequest extends ApiRequest {

    @NotBlank(message = "交易流水号[transNo]不能为空")
    private String transNo;

    private String merchantId;

    private String merchantName;

    private PayeeType payeeType;

    private List<SubstituteDetail<? extends Payee>> detail = new ArrayList<>();

    private String remark;

    private String notifyUrl;

    public long getTotalAmount() {
        return detail.stream()
                .mapToLong(SubstituteDetail::getAmount)
                .sum();
    }
}
