package org.hswebframework.payment.merchant.controller.request;

import org.hswebframework.payment.api.enums.PayeeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PayeeInfo {

    @NotBlank
    private String payeeInfoJson;

    @NotBlank
    private PayeeType payeeType;
}
