package org.hswebframework.payment;

import com.alibaba.fastjson.parser.ParserConfig;
import org.hswebframework.web.authorization.basic.configuration.EnableAopAuthorize;
import org.hswebframework.web.authorization.basic.web.ParsedToken;
import org.hswebframework.web.authorization.basic.web.SessionIdUserTokenParser;
import org.hswebframework.web.dao.Dao;
import org.hswebframework.web.loggin.aop.EnableAccessLogger;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SpringBootApplication
@EnableAopAuthorize
@EnableCaching
@EnableAsync
@EnableScheduling
@MapperScan(value = "org.hswebframework.payment", markerInterface = Dao.class)
@RestController
@EnableAccessLogger
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
        ParserConfig.getGlobalInstance().addAccept("com.hswebframework.payment");
        System.out.println("服务启动完成");
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SessionIdUserTokenParser sessionIdUserTokenParser() {
        return new SessionIdUserTokenParser() {
            @Override
            public ParsedToken parseToken(HttpServletRequest request) {
                return super.parseToken(request);
            }
        };
    }

}
