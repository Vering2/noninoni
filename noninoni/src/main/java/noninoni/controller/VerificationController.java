package noninoni.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import noninoni.service.EmailVerificationService;

@Controller
public class VerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam("token") String token, @RequestParam("email") String memberEmail,Model model) {
        // 토큰을 사용하여 이메일 인증
        boolean isVerified = emailVerificationService.verifyEmail(memberEmail, token);

        if (isVerified) {
            // 인증 성공 시의 로직
            return "redirect:/email-verified";
        } else {
            // 인증 실패 시의 로직
            return "redirect:/email-verification-failed";
        }
    }

    @GetMapping("/email-verified")
    public String emailVerified() {
        // 이메일 인증 성공 페이지
        return "fragments/email-verified";
    }

    @GetMapping("/email-verification-failed")
    public String emailVerificationFailed() {
        // 이메일 인증 실패 페이지
        return "fragments/email-verification-failed";
    }
}

