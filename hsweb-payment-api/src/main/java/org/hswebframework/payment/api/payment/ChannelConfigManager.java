package org.hswebframework.payment.api.payment;

import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ChannelConfigManager {

    <T extends ChannelConfig> Optional<T> getChannelConfigById(String channelConfigId, Class<T> tClass);
}
