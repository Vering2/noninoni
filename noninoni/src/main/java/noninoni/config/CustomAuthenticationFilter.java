package noninoni.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import noninoni.dto.CustomUserDetails;
import noninoni.service.MemberService;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class CustomAuthenticationFilter extends OncePerRequestFilter {
	
	@Autowired
	private MemberService memberService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String currentUrl = request.getRequestURI();
		String queryString = request.getQueryString(); // 쿼리 문자열 가져오기

		// 쿼리 문자열이 있으면 URL에 추가
		if (queryString != null) {
			currentUrl += "?" + queryString;
		}

		if (!isStaticResource(currentUrl)) {
			// 현재 URL을 쿠키에 저장
			Cookie lastPageCookie = new Cookie("lastPage", currentUrl);
			lastPageCookie.setMaxAge(60); // 예: 1 동안 유효
			lastPageCookie.setPath("/"); // 쿠키가 전체 도메인에서 유효하도록 설정

			response.addCookie(lastPageCookie);
		}
		// 세션 유효성 검사
		HttpSession session = request.getSession(false);
		if (session != null) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
				CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
				LocalDateTime lastLoginTime = memberService.getLastLoginTime(userDetails.getUsername());

				LocalDateTime sessionLoginTime = (LocalDateTime) session.getAttribute("LOGIN_TIME");
				if (sessionLoginTime == null || !sessionLoginTime.isEqual(lastLoginTime)) {
					SecurityContextHolder.clearContext(); // 보안 컨텍스트 초기화
					session.invalidate(); // 세션 무효화
					response.sendRedirect("/login?logout=other"); // 로그인 페이지로 리디렉션
					return;
				}
			}
		}
		filterChain.doFilter(request, response);
	}

	private boolean isStaticResource(String uri) {
		return uri.startsWith("/uploaded_files/") || uri.startsWith("/images/") || uri.startsWith("/css/")
				|| uri.startsWith("/js/") || uri.startsWith("/colors")
				|| uri.startsWith("/join") || uri.startsWith("/find-id") || uri.startsWith("/forgot-password")
				|| uri.startsWith("/reset-password") || uri.startsWith("/favicon")
				|| uri.startsWith("/cart/add-to-cart-and-checkout") || uri.startsWith("/login") || uri.contains("/size")
				|| uri.contains("/color") || uri.contains("/logout") || uri.contains("/checkout")
				|| uri.contains("/categoryProducts") || uri.contains("/api") || uri.startsWith("/validate-input");
	}
}
