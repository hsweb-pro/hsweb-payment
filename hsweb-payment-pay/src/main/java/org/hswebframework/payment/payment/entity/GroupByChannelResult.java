package org.hswebframework.payment.payment.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class GroupByChannelResult {
    private String channelName;

    private long total;

}
