package org.hswebframework.payment.api.message.email;


import org.springframework.mail.javamail.JavaMailSender;

public interface MailSenderProvider {

    String DEFAULT = "default";

    String DEV = "dev";

    String USER = "user";

    String getName();

    JavaMailSender getSender();
}
