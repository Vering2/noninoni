package noninoni.config;

import org.springframework.beans.factory.annotation.Autowired;
import noninoni.entity.Member;
import noninoni.entity.ProductVariant;
import noninoni.dto.CartItemDto;
import noninoni.dto.CustomUserDetails;
import noninoni.entity.Cart;
import noninoni.repository.MemberRepository;
import noninoni.service.CartService;
import noninoni.service.MemberService;
import noninoni.service.ProductService;
import noninoni.service.ProductVariantService;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductVariantService productVariantService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MemberService memberService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		HttpSession session = request.getSession();

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		memberService.updateLastLoginTime(userDetails.getUsername()); // 마지막 로그인 시간 업데이트
		// 업데이트된 마지막 로그인 시간을 다시 조회
		LocalDateTime lastLoginTime = memberService.getLastLoginTime(userDetails.getUsername());
		// 로그인 성공 후 세션에 로그인 시간을 저장
		session.setAttribute("LOGIN_TIME", lastLoginTime);

		// 현재 로그인한 사용자의 username 가져오기
		String username = authentication.getName();

		// MemberRepository를 통해 Member 엔티티 조회
		Member member = memberRepository.findByMemberId(username)
				.orElseThrow(() -> new IllegalArgumentException("Invalid username"));

		// 사용자의 장바구니 가져오기 또는 생성
		Cart cart = cartService.getOrCreateCart(member);
		session.setAttribute("cartId", cart.getId());

		Map<String, Integer> sessionCart = (Map<String, Integer>) session.getAttribute("cart");

		if (sessionCart != null && !sessionCart.isEmpty()) {
			List<CartItemDto> cartItems = new ArrayList<>();
			for (Map.Entry<String, Integer> entry : sessionCart.entrySet()) {
				String[] keys = entry.getKey().split(":");
				Long productId = Long.parseLong(keys[0]);
				Long colorId = Long.parseLong(keys[1]);
				Long sizeId = Long.parseLong(keys[2]);
				ProductVariant variant = productVariantService.getProductVariant(productId, colorId, sizeId);
				CartItemDto itemDto = new CartItemDto(productService.getProduct(productId), variant, entry.getValue());
				cartItems.add(itemDto);
			}
			// 장바구니 병합
			cartService.mergeCart(member, cartItems);
			session.removeAttribute("cart");
		}

		String lastPage = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("lastPage".equals(cookie.getName())) {
					lastPage = cookie.getValue();
					break;
				}
			}
		}
		response.sendRedirect(lastPage != null ? lastPage : "/");
	}
}
