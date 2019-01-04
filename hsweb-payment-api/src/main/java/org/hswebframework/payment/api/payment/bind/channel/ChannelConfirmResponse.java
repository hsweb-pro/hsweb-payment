package org.hswebframework.payment.api.payment.bind.channel;

import org.hswebframework.payment.api.ApiResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelConfirmResponse extends ApiResponse {


    private String authorizeCode;
}
