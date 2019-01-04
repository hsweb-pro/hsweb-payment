package org.hswebframework.payment.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.*;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.logging.entity.SystemLoggerEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.id.IDGenerator;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;

@Slf4j
public class LogbackSystemLoggerAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static ApplicationEventPublisher publisher;

    @Setter
    @Getter
    private String commitId = "unknown";

    static void setPublisher(ApplicationEventPublisher publisher) {
        LogbackSystemLoggerAppender.publisher = publisher;
    }

    private String getCurrentRequestId() {
        return Optional.ofNullable(MDC.get("requestId"))
                .orElseGet(() -> Optional.ofNullable(ThreadLocalUtils.<String>get("requestId")).orElse("none"));
    }

    private String getCurrentBusinessId() {
        return Optional.ofNullable(MDC.get("businessId"))
                .orElseGet(() -> Optional
                        .ofNullable(ThreadLocalUtils.get("businessId"))
                        .map(String.class::cast)
                        .orElse("none"));
    }

    private String getCurrentUserId() {
        return Optional.ofNullable(MDC.get("merchantId"))
                .orElseGet(() -> Authentication.current()
                        .map(Authentication::getUser)
                        .map(User::getId)
                        .orElseGet(() -> Optional.ofNullable(ThreadLocalUtils.<String>get("userId")).orElse("none")));
    }

    private String getCurrentUserName() {
        return Authentication.current()
                .map(Authentication::getUser)
                .map(User::getName)
                .orElse("none");
    }

    private String getCurrentSessionId() {
        return Authentication.current()
                .flatMap(autz -> autz.getAttribute("sessionId"))
                .map(String.class::cast)
                .orElseGet(this::getCurrentRequestId);
    }


    @Override
    protected void append(ILoggingEvent event) {
        if (publisher == null) {
            return;
        }
        StackTraceElement element = event.getCallerData()[0];
        IThrowableProxy proxies = event.getThrowableProxy();
        String message = event.getFormattedMessage();
        String stack = null;

        if (null != proxies) {
            int commonFrames = proxies.getCommonFrames();
            StackTraceElementProxy[] stepArray = proxies.getStackTraceElementProxyArray();
            StringJoiner joiner = new StringJoiner("\n", message + "\n[", "]");
            StringBuilder stringBuilder = new StringBuilder();
            ThrowableProxyUtil.subjoinFirstLine(stringBuilder, proxies);
            joiner.add(stringBuilder);
            for (int i = 0; i < stepArray.length - commonFrames; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(CoreConstants.TAB);
                ThrowableProxyUtil.subjoinSTEP(sb, stepArray[i]);
                joiner.add(sb);
            }
            stack = joiner.toString();
        } else {
            if (event.getLevel() != Level.INFO) {
                StringJoiner joiner = new StringJoiner("\n", message + "\n[", "]");
                for (StackTraceElement e : event.getCallerData()) {
                    joiner.add(e.toString());
                }
                stack = joiner.toString();
            }
        }

        SystemLoggerEntity info = SystemLoggerEntity.builder()
                .userId(getCurrentUserId())
                .userName(getCurrentUserName())
                .sessionId(getCurrentSessionId())
                .requestId(getCurrentRequestId())
                .businessId(getCurrentBusinessId())
                .level(event.getLevel().levelStr)
                .name(event.getLoggerName())
                .className(element.getClassName())
                .methodName(element.getMethodName())
                .lineNumber(element.getLineNumber())
                .message(message)
                .gitHash(commitId)
                .stackInfo(stack)
                .contextJson(JSON.toJSONString(MDC.getCopyOfContextMap()))
                .threadName(event.getThreadName())
                .createTime(new Date(event.getTimeStamp()))
                .build();
        info.setId(IDGenerator.MD5.generate());
        publisher.publishEvent(info);
    }
}
