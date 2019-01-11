package org.hswebframework.payment.api.message;

import org.hswebframework.payment.api.message.email.EmailSender;

public interface MessageSender {

    EmailSender email();
}
