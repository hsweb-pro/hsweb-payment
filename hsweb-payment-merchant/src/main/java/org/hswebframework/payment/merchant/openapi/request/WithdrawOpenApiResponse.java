package org.hswebframework.payment.merchant.openapi.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawOpenApiResponse {

    private boolean success;

    private String code;

    private String message;
}
