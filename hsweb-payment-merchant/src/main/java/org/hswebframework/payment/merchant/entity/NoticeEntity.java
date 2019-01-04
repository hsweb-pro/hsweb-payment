package org.hswebframework.payment.merchant.entity;

import org.hswebframework.payment.api.enums.NoticeStatus;
import org.hswebframework.payment.api.enums.NoticeType;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@Table(name = "sys_notice")
public class NoticeEntity extends SimpleGenericEntity<String> {

    @Column(name = "id")
    private String id;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "create_user")
    private String createUser;

    @Column(name = "update_user")
    private String updateUser;

    @Column(name = "content")
    private String content;

    @Column(name = "title")
    private String title;

    @Column(name = "types")
    private NoticeType[] types;

    @Column(name = "status")
    private NoticeStatus status = NoticeStatus.CLOSE;
}
