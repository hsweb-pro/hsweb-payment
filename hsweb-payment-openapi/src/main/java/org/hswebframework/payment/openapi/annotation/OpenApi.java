package org.hswebframework.payment.openapi.annotation;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
public @interface OpenApi {

    String value();

    /**
     * @return 接口名称
     */
    String name() default "";

    /**
     * @return 接口说明
     */
    String description() default "";

}
