package org.hswebframework.payment.api.message.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Getter
@Setter
public class EMailProperties {

    private Map<String, MailProperties> mails = new HashMap<>();

    public List<MailSenderProvider> buildProviders() {
        return mails.entrySet()
                .stream().map(entry -> {
                    String provider = entry.getKey();
                    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
                    applyProperties(javaMailSender, entry.getValue());
                    return new MailSenderProvider() {
                        @Override
                        public String getName() {
                            return provider;
                        }

                        @Override
                        public JavaMailSender getSender() {
                            return javaMailSender;
                        }
                    };
                }).collect(Collectors.toList());
    }


    private void applyProperties(JavaMailSenderImpl sender, MailProperties properties) {
        sender.setHost(properties.getHost());
        if (properties.getPort() != null) {
            sender.setPort(properties.getPort());
        }
        sender.setUsername(properties.getUsername());
        sender.setPassword(properties.getPassword());
        sender.setProtocol(properties.getProtocol());
        if (properties.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(properties.getDefaultEncoding().name());
        }
        if (!properties.getProperties().isEmpty()) {
            sender.setJavaMailProperties(asProperties(properties.getProperties()));
        }
    }

    private Properties asProperties(Map<String, String> source) {
        Properties properties = new Properties();
        properties.putAll(source);
        return properties;
    }

}
