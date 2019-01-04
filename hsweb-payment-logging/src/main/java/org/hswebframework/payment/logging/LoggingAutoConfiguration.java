package org.hswebframework.payment.logging;

import lombok.SneakyThrows;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class LoggingAutoConfiguration implements ApplicationEventPublisherAware {
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        LogbackSystemLoggerAppender.setPublisher(applicationEventPublisher);
    }

    @Bean
    @SneakyThrows
    public DbSearcher dbSearcher() {
        DbConfig config = new DbConfig();
//        try (FileOutputStream fileOutputStream = new FileOutputStream("./data/ip2region.db")) {
//            StreamUtils.copy(LoggingAutoConfiguration.class.getResourceAsStream("/ip/ip2region.db"), fileOutputStream);
//        }

        DbSearcher searcher = new DbSearcher(config, "./ip2region.db");

        return searcher;

    }
}
