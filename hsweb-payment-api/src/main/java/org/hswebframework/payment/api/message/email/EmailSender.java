package org.hswebframework.payment.api.message.email;

import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface EmailSender {

    JavaMailSender get(String provider, boolean useDefault);


    default JavaMailSender getDefault() {
        return get("default", true);
    }
}
