package org.hswebframework.payment.api.payment.bind;

import org.hswebframework.payment.api.ApiResponse;
import lombok.Getter;
import lombok.Setter;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class BindCardResponse extends ApiResponse {
    private String bindId;

}
