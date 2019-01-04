package org.hswebframework.payment;

import org.hswebframework.payment.api.utils.IPUtils;
import org.hswebframework.web.WebUtil;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhouhao
 * @since
 */
@RestController
public class IpController {

    @GetMapping("/ip")
    public ResponseMessage<String> getId(HttpServletRequest request) {
        return ResponseMessage.ok(IPUtils.getRealIp(WebUtil.getIpAddr(request)));
    }
}
