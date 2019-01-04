package org.hswebframework.payment.openapi.docs;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ErrorCode {

    private String code;

    private String message;

    private String description;
}
