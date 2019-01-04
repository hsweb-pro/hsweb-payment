package org.hswebframework.payment.payment.service.impl;

import org.hswebframework.payment.payment.dao.NotificationLogDao;
import org.hswebframework.payment.payment.entity.NotificationLogEntity;
import org.hswebframework.payment.payment.events.NotificationSuccessEvent;
import org.hswebframework.payment.payment.notify.Notification;
import org.hswebframework.payment.payment.notify.NotifyResult;
import org.hswebframework.payment.payment.notify.PaymentNotifier;
import lombok.SneakyThrows;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
public class LocalNotifyLogService extends GenericEntityService<NotificationLogEntity, String> {

    @Autowired
    private NotificationLogDao notificationLogDao;

    @Autowired
    private PaymentNotifier paymentNotifier;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public NotificationLogDao getDao() {
        return notificationLogDao;
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @SneakyThrows
    public NotifyResult retryNotify(String logId) {
        NotificationLogEntity entity = selectByPk(logId);
        if (entity == null) {
            throw new NotFoundException("通知记录不存在");
        }
        try (MDC.MDCCloseable closeable = MDC.putCloseable("businessId", entity.getPaymentId())) {

            entity.setLastNotifyTime(new Date());
            Notification notification = entity.copyTo(new Notification());
            notification.setStatus(entity.getPaymentStatus());
            NotifyResult result = paymentNotifier
                    .createNotifier(entity.getNotifyType(), notification)
                    .doNotify();

            if (result.isSuccess()) {
                entity.setNotifySuccess(result.isSuccess());
                createUpdate()
                        .set(entity::getNotifySuccess)
                        .set(entity::getLastNotifyTime)
                        .where("id", logId)
                        .exec();
                eventPublisher.publishEvent(new NotificationSuccessEvent(entity.getPaymentId()));
            } else {
                entity.setErrorReason(result.getErrorReason());
                createUpdate()
                        .set(entity::getErrorReason)
                        .where("id", logId)
                        .exec();
            }
            return result;
        }

    }
}
