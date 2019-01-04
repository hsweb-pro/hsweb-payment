package org.hswebframework.payment.openapi.docs;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ApiParameter {
    private String id;

    private String name;

    private String parameterType;

    private String type;

    private String required = "否";

    private String enc = "否";

    private String description = "";

    private String example = "";


}
