package org.hswebframework.payment.openapi;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.bean.FastBeanCopier;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class OpenApiRequest {

    @NotBlank(message = "参数[requestId]不能为空")
    private String requestId;

    @NotBlank(message = "参数[merchantId]不能为空")
    private String merchantId;

    @NotBlank(message = "参数[sign]不能为空")
    private String sign;

//    @NotBlank(message = "参数[nonce]不能为空")
//    private String nonce;
//
//    @NotBlank(message = "参数[timestamp]不能为空")
//    private String timestamp;

}
