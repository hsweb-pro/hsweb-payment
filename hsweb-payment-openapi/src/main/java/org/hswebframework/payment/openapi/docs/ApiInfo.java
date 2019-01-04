package org.hswebframework.payment.openapi.docs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ApiInfo {

    private String id;

    private String group;

    private String name;

    private String httpMethod;

    private String contentType;

    private String description;

    private List<ApiParameter> commonRequestParams;

    private List<ApiParameter> requestParams;

    private List<ApiParameter> commonResponseParams;

    private List<ApiParameter> responseParams;

    private List<ErrorCode> errorCodes;

    private List<Env> envs;
}
