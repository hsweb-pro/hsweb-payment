package org.hswebframework.payment.api.message;

import org.hswebframework.payment.api.message.email.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
public class DefaultMessageSender implements MessageSender {

    @Autowired
    private EmailSender emailSender;

    @Override
    public EmailSender email() {
        return emailSender;
    }
}
