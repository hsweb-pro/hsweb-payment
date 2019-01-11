package org.hswebframework.payment.api.events;

import java.io.Serializable;

/**
 * 所有业务相关的事件都应该实现此接口.
 * 注意:在监听事件的时候,不要抛出异常.
 *
 * @author zhouhao
 * @see org.springframework.context.ApplicationEventPublisher
 * @see java.util.EventListener
 * @see org.springframework.transaction.event.TransactionalEventListener
 * @see org.springframework.scheduling.annotation.Async
 * @since 1.0.0
 */
public interface BusinessEvent extends Serializable {
}
