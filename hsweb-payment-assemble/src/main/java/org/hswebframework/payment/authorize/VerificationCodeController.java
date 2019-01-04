package org.hswebframework.payment.authorize;

import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.IIOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class VerificationCodeController {

    @GetMapping("/verify-code")
    @SneakyThrows
    public void requestVerifyCode(HttpSession session, HttpServletResponse response) {
        VerificationCode code = new VerificationCode();
        session.setAttribute("verifyCode", code.getCode());
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        try {
            code.write(response.getOutputStream());
        } catch (IIOException e) {
            //ignore
        }
    }
}
