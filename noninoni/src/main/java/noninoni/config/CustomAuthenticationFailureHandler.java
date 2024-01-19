package noninoni.config;

import java.io.IOException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage;

        if (exception instanceof DisabledException) {
            // 계정이 비활성화된 경우
            errorMessage = "이메일 인증이 필요합니다.";
        } else {
            // 그 외 일반적인 로그인 실패
            errorMessage = "아이디 또는 비밀번호가 맞지 않습니다.";
        }

        request.getSession().setAttribute("loginError", errorMessage);
        response.sendRedirect("/login");
    }
}
