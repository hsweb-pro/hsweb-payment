package org.hswebframework.payment.api.payment.quick;

import org.hswebframework.payment.api.payment.PaymentRequest;
import lombok.*;


/**
 * 快捷支付请求
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuickPaymentRequest extends PaymentRequest {
    //绑卡流水号
    private String bindId;

    private String accountName;

    private String accountNumber;

    private String phoneNumber;

    private String idNumber;

    private String validDate;

    private String cvn2;

    private String remarks;

}
