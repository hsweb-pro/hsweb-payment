package org.hswebframework.payment.api;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.id.IDGenerator;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Setter
@Getter
public class ApiRequest implements Serializable {

    private String requestId;

    private long requestTime;

    public long getRequestTime() {
        if (requestTime <= 0) {
            requestTime = System.currentTimeMillis();
        }
        return requestTime;
    }

    public String getRequestId() {
        if (null == requestId) {
            requestId = IDGenerator.SNOW_FLAKE_STRING.generate();
        }
        return requestId;
    }
}
