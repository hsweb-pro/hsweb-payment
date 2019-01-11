package org.hswebframework.payment.api.message.email;

import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashMap;
import java.util.Map;

public class DefaultMailSender implements EmailSender {

    private Map<String, JavaMailSender> repo = new HashMap<>();

    @Override
    public JavaMailSender get(String provider, boolean useDefault) {
        JavaMailSender sender = repo.getOrDefault(provider, repo.get("default"));
        if (sender == null) {
            throw new UnsupportedOperationException("不支持此邮件发送:" + provider);
        }
        return sender;
    }

    protected void addSender(String provider, JavaMailSender javaMailSender) {
        repo.put(provider, javaMailSender);
    }

}
