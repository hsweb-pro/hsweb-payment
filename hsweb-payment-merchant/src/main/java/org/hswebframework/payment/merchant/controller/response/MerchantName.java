package org.hswebframework.payment.merchant.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@Builder
public class MerchantName {

    private String id;

    private String name;
}
