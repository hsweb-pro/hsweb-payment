package org.hswebframework.payment.merchant.openapi.request;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class SubstituteOpenApiResponse {
    private boolean success;

    private String transferId;

}
