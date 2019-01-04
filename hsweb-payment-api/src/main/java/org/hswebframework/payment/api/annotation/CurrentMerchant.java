package org.hswebframework.payment.api.annotation;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CurrentMerchant {
    boolean agent() default false;

    boolean agentOrMerchant() default false;
}
