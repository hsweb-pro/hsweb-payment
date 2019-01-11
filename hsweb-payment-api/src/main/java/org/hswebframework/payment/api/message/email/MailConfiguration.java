package org.hswebframework.payment.api.message.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(EmailSender.class)
public class MailConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "hsweb.message")
    public EMailProperties hfqMailProperties() {
        return new EMailProperties();
    }

    @Bean
    public EmailSender mailSenders(EMailProperties properties) {
        DefaultMailSender senders = new DefaultMailSender();
        for (MailSenderProvider mailSenderProvider : properties.buildProviders()) {
            senders.addSender(mailSenderProvider.getName(), mailSenderProvider.getSender());
        }
        return senders;
    }
}
