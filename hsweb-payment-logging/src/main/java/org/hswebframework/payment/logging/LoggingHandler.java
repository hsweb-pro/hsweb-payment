package org.hswebframework.payment.logging;

import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.api.enums.UserLogType;
import org.hswebframework.payment.api.utils.IPUtils;
import org.hswebframework.payment.logging.dao.AccessLoggerDao;
import org.hswebframework.payment.logging.dao.SystemLoggerDao;
import org.hswebframework.payment.logging.dao.UserOperationLoggerDao;
import org.hswebframework.payment.logging.entity.AccessLoggerEntity;
import org.hswebframework.payment.logging.entity.SystemLoggerEntity;
import org.hswebframework.payment.logging.entity.UserOperationLoggerEntity;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.listener.event.AuthorizationSuccessEvent;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.logging.AccessLoggerInfo;
import org.hswebframework.web.logging.events.AccessLoggerAfterEvent;
import org.hswebframework.web.logging.events.AccessLoggerBeforeEvent;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbSearcher;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Component
public class LoggingHandler {

    private BlockingQueue<Runnable> runnables = new LinkedBlockingQueue<>();

    @Autowired
    private SystemLoggerDao systemLoggerDao;

    @Autowired
    private UserOperationLoggerDao loginLoggerDao;

    @Autowired
    private AccessLoggerDao accessLoggerDao;

    private List<String> accessLoggerExcludes = Arrays.asList(
            "org.hswebframework.payment.logging.**",
            "**.AuthorizationController.authorize*"
    );

    @Autowired
    private DbSearcher dbSearcher;

    private static final PathMatcher pathMatcher = new AntPathMatcher(".");

    private volatile boolean running = true;

    @PreDestroy
    public void stop() {
        running = false;
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < Runtime.getRuntime().availableProcessors() / 2; i++) {
            new Thread(() -> {
                while (running) {
                    try {
                        runnables.take().run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @EventListener
    public void handleLoggingEvent(SystemLoggerEntity entity) {
        runnables.add(() -> systemLoggerDao.insert(entity));

    }

    @EventListener
    public void handleUserLoginEvent(AuthorizationSuccessEvent event) {
        String userId = event.getAuthentication().getUser().getId();
        String sessionId = IDGenerator.MD5.generate();
        event.getAuthentication().setAttribute("sessionId", sessionId);
        UserOperationLoggerEntity loggerEntity = new UserOperationLoggerEntity();
        loggerEntity.setIpAddress(WebUtil.getIpAddr(WebUtil.getHttpServletRequest()));
        loggerEntity.setName(event.getAuthentication().getUser().getName());
        loggerEntity.setUserId(userId);
        loggerEntity.setRequestTime(new Date());
        loggerEntity.setSessionId(sessionId);
        loggerEntity.setType(UserLogType.LOGIN);
        loggerEntity.setIpLocation(ipToRegion(loggerEntity.getIpAddress()));
        loggerEntity.setId(IDGenerator.MD5.generate());

        runnables.add(() -> loginLoggerDao.insert(loggerEntity));

    }

    @EventListener
    public void handleAccessLoggingEvent(AccessLoggerBeforeEvent entity) {
        AccessLoggerInfo loggerInfo = entity.getLogger();
        MDC.put("requestId", getRequestId(loggerInfo));
    }

    protected String getRequestId(AccessLoggerInfo accessLoggerInfo) {
        return Optional.ofNullable(MDC.get("requestId"))
                .orElse(accessLoggerInfo.getId());
    }

    private static final Class excludes[] = {
            ServletRequest.class,
            ServletResponse.class,
            InputStream.class,
            OutputStream.class,
            MultipartFile.class,
            MultipartFile[].class
    };

    private String ipToRegion(String ip) {
        try {
            return Optional.ofNullable(dbSearcher.memorySearch(IPUtils.getRealIp(ip)))
                    .map(DataBlock::getRegion)
                    .map(str -> str.split("[|]"))
                    .map(arr -> {
                        StringJoiner joiner = new StringJoiner(",");
                        for (int i = 0; i < arr.length; i++) {
                            String data = arr[i];
                            if ("0".equals(data)) {
                                continue;
                            }
                            joiner.add(data);
                        }
                        return joiner.toString();
                    }).orElse("-");
        } catch (Exception e) {
            System.out.println("根据ip获取地区失败:" + e.getMessage());
            return "-";
        }
    }

    @EventListener
    public void handleAccessLoggingAfterEvent(AccessLoggerAfterEvent entity) {
        AccessLoggerInfo loggerInfo = entity.getLogger();
        String path = loggerInfo.getTarget().getName() + "." + loggerInfo.getMethod().getName();

        for (String exclude : accessLoggerExcludes) {
            if (pathMatcher.match(exclude, path)) {
                return;
            }
        }
        String requestId = getRequestId(loggerInfo);
        AccessLoggerEntity loggerEntity = AccessLoggerEntity.builder().build();
        loggerEntity.setId(loggerInfo.getId());
        loggerEntity.setClassName(loggerInfo.getTarget().getName());
        loggerEntity.setMethodName(loggerInfo.getMethod().getName());
        loggerEntity.setRequestId(requestId);
        loggerEntity.setAction(loggerInfo.getAction());
        loggerEntity.setHttpHeader(JSON.toJSONString(loggerInfo.getHttpHeaders()));
        loggerEntity.setHttpMethod(loggerInfo.getHttpMethod());
        loggerEntity.setRequestTime(new Date(loggerInfo.getRequestTime()));
        loggerEntity.setResponseTime(new Date(loggerInfo.getResponseTime()));
        loggerEntity.setUseTime(loggerEntity.getUseTime());
        loggerEntity.setUrl(loggerInfo.getUrl());
        loggerEntity.setIpAddress(loggerInfo.getIp());
        loggerEntity.setSessionId("none");
        loggerEntity.setUserName("none");
        loggerEntity.setUserId("none");
        loggerEntity.setIpLocation(ipToRegion(loggerEntity.getIpAddress()));
        loggerEntity.setUseTime(loggerInfo.getResponseTime() - loggerInfo.getRequestTime());
        Authentication.current().ifPresent(autz -> {
            String sessionId = autz.getAttribute("sessionId").map(String.class::cast).orElse("none");
            loggerEntity.setSessionId(sessionId);
            loggerEntity.setUserId(autz.getUser().getId());
            loggerEntity.setUserName(autz.getUser().getName());
        });

        Map<String, Object> newParameter = new HashMap<>(loggerInfo.getParameters());

        for (Map.Entry<String, Object> entry : newParameter.entrySet()) {
            if (Stream.of(excludes).anyMatch(aClass -> aClass.isInstance(entry.getValue()))) {
                entry.setValue(entry.getValue().getClass().getSimpleName());
            }
        }
        if (loggerInfo.getException() != null) {
            loggerEntity.setErrorStack(StringUtils.throwable2String(loggerInfo.getException()));
        }
        loggerEntity.setParameters(JSON.toJSONString(newParameter));

        runnables.add(() -> accessLoggerDao.insert(loggerEntity));

    }


}
