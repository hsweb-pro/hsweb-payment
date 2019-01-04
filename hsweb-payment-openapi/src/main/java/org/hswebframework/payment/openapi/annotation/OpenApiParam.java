package org.hswebframework.payment.openapi.annotation;

import org.hswebframework.payment.api.crypto.Cipher;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpenApiParam {

    String value() default "";

    String name() default "";

    String[] description() default "";

    String example() default "";

    /**
     * @return 加密方式
     */
    Cipher.Type cipher() default Cipher.Type.NONE;

}
