package org.hswebframework.payment.api.payment.bind.channel;

import org.hswebframework.payment.api.ApiResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelBindCardResponse extends ApiResponse {

    private String channelId;

    private String bindConfirmCode;
}
