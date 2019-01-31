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
@Table(name = "log_sys_logger")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLoggerEntity extends SimpleGenericEntity<String> {

    @Column(name = "name")
    private String name;

    @Column(name = "module_info")
    private String moduleInfo;

    @Column(name = "thread_name")
    private String threadName;

    @Column(name = "class_name")
    private String className;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "message")
    private String message;

    @Column(name = "stack_info")
    private String stackInfo;

    @Column(name = "line_number")
    private int lineNumber;

    @Column(name = "git_hash")
    private String gitHash;

    @Column(name = "git_location")
    private String gitLocation;

    @Column(name = "level")
    private String level;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "business_id")
    private String businessId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "context_json")
    private String contextJson;
}
