package org.hswebframework.payment.merchant.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
public class PayeeFormInfo {

    private String name;

    private String payeeType;

    private List<PayeeConfigProperty> properties = new ArrayList<>();

}
