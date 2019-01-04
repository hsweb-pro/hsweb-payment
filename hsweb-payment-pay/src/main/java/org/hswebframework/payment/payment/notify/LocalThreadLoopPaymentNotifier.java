package org.hswebframework.payment.payment.notify;

import org.hswebframework.payment.api.enums.NotifyType;
import org.hswebframework.payment.payment.dao.NotificationLogDao;
import org.hswebframework.payment.payment.entity.NotificationLogEntity;
import org.hswebframework.payment.payment.events.NotificationSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

import static org.hswebframework.web.service.DefaultDSLUpdateService.*;

/**
 * 使用本地线程循环队列来进行异步通知,此类不支持在集群下使用
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j(topic = "system.payment.notify")
@Component
public class LocalThreadLoopPaymentNotifier implements PaymentNotifier, BeanPostProcessor, CommandLineRunner {

    private BlockingQueue<NotificationJob> notifyQueue = new LinkedBlockingQueue<>();

    private Map<NotifyType, NotifierProvider> providers = new ConcurrentHashMap<>();

    private ScheduledExecutorService executorService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private NotificationLogDao logDao;

    //重试间隔,单位秒
    private int[] delays = {0, 10, 30, 100, 600};

    private boolean running = true;

    @Override
    public Notifier createNotifier(NotifyType type, Notification content) {
        NotifierProvider provider = providers.get(type);
        if (provider != null) {
            return provider.createNotifier(content);
        }
        throw new UnsupportedOperationException("不支持的通知类型:" + type);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void doNotify(NotifyType type, Notification content) {
        try {
            NotifierProvider provider = providers.get(type);
            if (provider != null) {
                Notifier notifier = provider.createNotifier(content);

                //记录通知日志
                NotificationLogEntity logEntity = new NotificationLogEntity().copyFrom(content);
                logEntity.setNotifyType(type);
                logEntity.setId(IDGenerator.SNOW_FLAKE_STRING.generate());
                logEntity.setPaymentStatus(content.getStatus());
                logEntity.setNotifySuccess(false);
                logEntity.setRetryTimes(0);
                logEntity.setLastNotifyTime(new Date());
                logDao.insert(logEntity);
                startNotify(notifier, content, logEntity.getId(), 0);

            } else {
                log.error("unsupported notify type:{},all support type:{}", type, providers.keySet());
            }
        } catch (Throwable e) {
            log.error("发起通知商户失败,paymentId:{}", content.getPaymentId(), e);
        }
    }

    protected void startNotify(Notifier notifier, Notification notification, String logId, int retryTimes) {
        NotificationJob runnable = new NotificationJob();
        runnable.notifier = notifier;
        runnable.notification = notification;
        runnable.logId = logId;
        runnable.retryTimes = retryTimes;
        //添加到通知队列中
        notifyQueue.add(runnable);
    }

    @PreDestroy
    public void shutdown() {
        running = false;
    }

    @PostConstruct
    public void startup() {
        int processors = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newScheduledThreadPool(processors);
        //使用一半cpu来进行queue消费
        for (int i = 0, size = Math.max(1, processors / 2); i < size; i++) {
            executorService.submit(new NotificationRunnable());
        }

    }

    @Override
    public void run(String... args) {
        // 重启未通知的记录
        DefaultDSLQueryService.createQuery(logDao)
                .where("notifySuccess", false)
                .and()
                .lt("retryTimes", 10)
                .listNoPaging()
                .forEach(notificationLogEntity -> {
                    NotifierProvider provider = providers.get(notificationLogEntity.getNotifyType());
                    if (provider != null) {
                        Notification notification = notificationLogEntity.copyTo(new Notification());
                        notification.setStatus(notificationLogEntity.getPaymentStatus());
                        startNotify(provider.createNotifier(notification), notification, notificationLogEntity.getId(), notificationLogEntity.getRetryTimes());
                    } else {
                        log.warn("不支持的通知类型:{}", notificationLogEntity.getNotifyType());
                    }
                });
    }

    class NotificationRunnable implements Runnable {

        @Override
        public void run() {
            for (; running; ) {
                try {
                    //消费通知队列
                    NotificationJob job = notifyQueue.take();
                    MDC.putCloseable("businessId", job.notification.getPaymentId());
                    //延迟
                    int delay = delays[Math.min(job.retryTimes, delays.length - 1)];
                    log.info("{}秒后开始执行交易通知[{}]", delay, job.notification.getPaymentId());
                    executorService.schedule(() -> {
                        try (MDC.MDCCloseable closeable = MDC.putCloseable("businessId", job.notification.getPaymentId())) {
                            if (job.retryTimes++ > 0 && !job.onRetryBefore()) {
                                log.info("结束[{}]通知,已重试次数:{}", job.notification.getPaymentId(), job.retryTimes);
                                //主动停止了通知
                                return;
                            }
                            NotifyResult result = job.lastResult = job.notifier.doNotify();
                            if (!result.isSuccess()) {
                                job.onError();
                            }
                            //执行通知
                            if (result.isSuccess() && job.onSuccess()) {
                                //成功,结束通知
                                job.notifier.cleanup();
                                //推送事件
                                publisher.publishEvent(new NotificationSuccessEvent(job.notification.getPaymentId()));
                                return;
                            }
                        } catch (Exception e) {
                            log.error("通知失败:{}", e.getMessage(), e);
                        }
                        //重新入队
                        notifyQueue.add(job);
                    }, delay, TimeUnit.SECONDS);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    MDC.clear();
                }
            }
        }
    }

    class NotificationJob {
        private String logId;
        private Notification notification;
        private NotifyResult lastResult;
        private Notifier notifier;
        private volatile int retryTimes;

        boolean onRetryBefore() {
            if (retryTimes > 10) {
                return false;
            }
            //如果记录里还已经成功则停止通知
            return createUpdate(logDao)
                    .set("retryTimes", retryTimes)
                    .set("lastNotifyTime", new Date())
                    .where("id", logId)
                    .and("notifySuccess", false)
                    .exec() == 1;
        }

        boolean onError() {
            createUpdate(logDao)
                    .set("errorReason", lastResult.getErrorReason())
                    .where("id", logId)
                    .exec();
            return true;
        }

        boolean onSuccess() {
            createUpdate(logDao)
                    .set("notifySuccess", true)
                    .where("id", logId)
                    .exec();
            return true;
        }

    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof NotifierProvider) {
            NotifierProvider provider = ((NotifierProvider) bean);
            providers.put(provider.getType(), provider);
        }
        return bean;
    }
}
