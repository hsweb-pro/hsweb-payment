package org.hswebframework.payment.logging.entity;

import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Table(name = "log_access_logger")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessLoggerEntity extends SimpleGenericEntity<String> {

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "url")
    private String url;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "ip_location")
    private String ipLocation;


    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "http_header")
    private String httpHeader;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "parameters")
    private String parameters;

    @Column(name = "error_stack")
    private String errorStack;

    @Column(name = "class_name")
    private String className;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "action")
    private String action;

    @Column(name = "request_time")
    private Date requestTime;

    @Column(name = "response_time")
    private Date responseTime;

    @Column(name = "use_time")
    private long useTime;

}
