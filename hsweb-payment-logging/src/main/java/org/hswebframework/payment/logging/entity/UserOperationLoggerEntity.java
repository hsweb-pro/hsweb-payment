package org.hswebframework.payment.logging.entity;

import org.hswebframework.payment.api.enums.UserLogType;
import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * 用户关键操作日志
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Table(name = "log_user_operation")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOperationLoggerEntity extends SimpleGenericEntity<String> {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "name")
    private String name;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "type")
    private UserLogType type;

    @Column(name = "request_time")
    private Date requestTime;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "ip_location")
    private String ipLocation;

}
