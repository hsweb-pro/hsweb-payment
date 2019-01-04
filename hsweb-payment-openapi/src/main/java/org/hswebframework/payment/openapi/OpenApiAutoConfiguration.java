package org.hswebframework.payment.openapi;

import org.hswebframework.payment.openapi.resolver.OpenApiRequestResponseResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class OpenApiAutoConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebMvcConfigurer openApiInterceptorConfigurer(OpenApiRequestResponseResolver resolver) {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(resolver);
                super.addInterceptors(registry);
            }
        };
    }
}
