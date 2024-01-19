package noninoni.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import noninoni.dto.CartItemDto;
import noninoni.dto.CustomUserDetails;
import noninoni.dto.MemberDTO;
import noninoni.entity.DeliveryAddress;
import noninoni.entity.MainCategory;
import noninoni.entity.Member;
import noninoni.entity.Product;
import noninoni.entity.ProductVariant;
import noninoni.repository.MainCategoryRepository;
import noninoni.repository.MemberRepository;
import noninoni.service.CartService;
import noninoni.service.DeliveryAddressService;
import noninoni.service.EmailVerificationService;
import noninoni.service.FavoriteService;
import noninoni.service.MemberService;
import noninoni.service.ProductService;
import noninoni.service.ProductVariantService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ViewController extends BaseController {
	@Autowired
	private MemberService memberService;

	@Autowired
	private MainCategoryRepository mainCategoryRepository;

	@Autowired
	private FavoriteService favoriteService;
	
	@Autowired
	private EmailVerificationService emailVerificationService;

	@GetMapping("/")
	public String index(Model model) {
		// 여기서 필요한 초기화 작업을 수행하고
		// 예를 들어 addCommonAttributes(model)를 호출하여 공통 속성을 추가할 수 있습니다.

		// 그리고 메인 페이지로 리다이렉션합니다.
		return "redirect:/home";
	}

	@GetMapping("/home")
	public String home(Model model, @RequestParam Map<String, String> allParams) {
		List<MainCategory> sortedMainCategories = mainCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "number"));
		Map<String, Page<Product>> categoryProductsMap = new LinkedHashMap<>();

		for (MainCategory category : sortedMainCategories) {
			String categoryType = category.getType();
			PageRequest pageRequest = PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "id"));
			Page<Product> productsPage = productService.getProductsBymainCategory(categoryType, pageRequest);
			categoryProductsMap.put(categoryType, productsPage);
		}

		model.addAttribute("categoryProductsMap", categoryProductsMap);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String memberId = userDetails.getUsername();
			List<Long> favoriteProductIds = favoriteService.getFavoriteProductIdsByMemberId(memberId);
			model.addAttribute("favoriteProductIds", favoriteProductIds);
		}

		return "home";
	}

	@GetMapping("/join")
	public String join(Model model) {
		return "contents/join";
	}

	// 회원 가입 컨트롤러

	@PostMapping("/signup")
	public String signupMember(MemberDTO memberDTO) {
		// 여기에서 필요한 유효성 검사 등을 추가할 수 있습니다.

		memberService.signupMember(memberDTO);

		// 이메일 인증 메일 전송
		emailVerificationService.sendVerificationEmail(memberDTO.getEmail());

		return "fragments/signup-success";
	}

	@GetMapping("/login")
	public String loginPage(HttpSession session, Model model) {
		if (session.getAttribute("loginError") != null) {
			model.addAttribute("loginError", session.getAttribute("loginError"));
			session.removeAttribute("loginError");
		}

		return "contents/login";
	}

	@GetMapping("/agreement1")
	public String agreement1(Model model) {

		return "fragments/agreement1";
	}

	@GetMapping("/agreement2")
	public String agreement2(Model model) {

		return "fragments/agreement2";
	}

	@GetMapping("/find-id")
	public String showFindIdForm(Model model) {

		return "contents/find-id";
	}

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	// 비밀번호 확인 페이지
	@GetMapping("/password-confirmation")
	public String passwordConfirmation(Principal principal, RedirectAttributes redirectAttributes, Model model) {

		if (principal != null) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
				CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
				String provider = userDetails.getProvider(); // 소셜 로그인 제공자 확인

				// 소셜 로그인의 경우 비밀번호 확인 없이 회원정보 수정 페이지로 리다이렉션
				if ("google".equals(provider) || "naver".equals(provider) || "kakao".equals(provider)) {
					return "redirect:/member-edit";
				}
			}
		}

		// 일반 로그인 사용자의 경우 비밀번호 확인 페이지 표시
		return "contents/password-confirmation";
	}

	// 비밀번호 확인 처리
	@PostMapping("/check-password")
	public String checkPassword(@RequestParam("password") String password, Principal principal,
			RedirectAttributes attributes, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			Member member = memberRepository.findByMemberId(userDetails.getUsername())
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));
			if (passwordEncoder.matches(password, member.getPassword())) {
				return "redirect:/member-edit";
			} else {
				attributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
				return "redirect:/password-confirmation";
			}
		}
		return "";

	}

	// 회원정보 수정 페이지
	@GetMapping("/member-edit")
	public String memberEdit(Model model, Principal principal) {
		Member member = memberRepository.findByMemberId(principal.getName()).orElse(null);

		if (member == null) {
			return "redirect:/home";
		}

		model.addAttribute("member", member);

		String fullMobileNumber = member.getMobile();
		if (fullMobileNumber != null) {
			String[] parts = fullMobileNumber.split(",");
			// 각 부분을 모델에 추가
			model.addAttribute("mobile1", parts.length > 0 ? parts[0] : "");
			model.addAttribute("mobile2", parts.length > 1 ? parts[1] : "");
			model.addAttribute("mobile3", parts.length > 2 ? parts[2] : "");
			// 나머지 코드
		}

		return "contents/member-edit";
	}

	// 회원정보 수정 처리
	@PostMapping("/update-member-info")
	public String updateMemberInfo(Member updatedMember) {
		memberService.updateMemberInfo(updatedMember);
		// 회원정보 업데이트 로직 (비밀번호 암호화 및 DB 저장)
		return "redirect:/mypage"; // 수정 후 리다이렉션할 페이지
	}

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductVariantService productVariantService;
	@Autowired
	private ProductService productService;

	@GetMapping("/cart")
	public String viewCart(Model model, HttpServletRequest request) {
		HttpSession session = request.getSession();
		CustomUserDetails user = getCurrentUser();

		List<CartItemDto> cartItems = new ArrayList<>();

		if (user != null) {
			// 로그인한 사용자: 데이터베이스에서 장바구니 정보 가져오기
			Long cartId = (Long) session.getAttribute("cartId");
			if (cartId != null) {
				cartItems = cartService.getCartItemsWithProductDetails(cartId);
			}
		} else {
			// 비로그인 사용자: 세션에서 장바구니 정보 가져오기
			Map<String, Integer> sessionCart = (Map<String, Integer>) session.getAttribute("cart");
			if (sessionCart != null && !sessionCart.isEmpty()) {
				for (Map.Entry<String, Integer> entry : sessionCart.entrySet()) {
					String[] keys = entry.getKey().split(":");
					Long productId = Long.parseLong(keys[0]);
					Long colorId = Long.parseLong(keys[1]);
					Long sizeId = Long.parseLong(keys[2]);

					ProductVariant variant = productVariantService.getProductVariant(productId, colorId, sizeId);

					CartItemDto itemDto = new CartItemDto(productService.getProduct(productId), variant,
							entry.getValue());
					cartItems.add(itemDto);
				}
			}
		}

		model.addAttribute("cartItems", cartItems);
		return "contents/cart";
	}

	private String getMemberIdFromAuthentication(Authentication authentication) {
		return authentication != null && authentication.getPrincipal() instanceof CustomUserDetails
				? ((CustomUserDetails) authentication.getPrincipal()).getUsername()
				: null;
	}

	@Autowired
	private ExecutorService executorService; // ExecutorService를 Bean으로 등록하고 주입
	@Autowired
	private DeliveryAddressService deliveryAddressService;

	@GetMapping("/shippingaddress")
	public String shippingaddress(Model model, HttpServletRequest request, Authentication authentication)
			throws Exception {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		String memberId = getMemberIdFromAuthentication(authentication);

		// 비동기 작업 시작
		CompletableFuture<List<DeliveryAddress>> addressesFuture = CompletableFuture
				.supplyAsync(() -> deliveryAddressService.getMemberDeliveryAddresses(memberId), executorService);

		CompletableFuture<DeliveryAddress> defaultAddressFuture = CompletableFuture.supplyAsync(() -> {
			Member member = memberService.findMemberById(memberId);
			return member != null && member.getDefaultAddress() != null
					? deliveryAddressService.getDefaultaddress(member.getDefaultAddress())
					: null;
		}, executorService);

		// 모든 비동기 작업이 완료될 때까지 기다립니다.
		CompletableFuture.allOf(addressesFuture, defaultAddressFuture).join();

		List<DeliveryAddress> addresses = addressesFuture.get();
		DeliveryAddress defaultAddress = defaultAddressFuture.get();
		model.addAttribute("deliveryAddresses", addresses);
		model.addAttribute("defaultaddress", defaultAddress);
		return "contents/shippingaddress";
	}

	@GetMapping("/admin/members")
	public String viewMembers(@RequestParam Optional<String> keyword, @RequestParam(defaultValue = "0") int page,
			Model model) {
		final int PAGE_SIZE = 10; // 한 페이지에 표시할 회원 수
		Page<Member> membersPage = memberService.getMembersPage(keyword, page, PAGE_SIZE);

		model.addAttribute("membersPage", membersPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", membersPage.getTotalPages());
		model.addAttribute("startPage", Math.max(0, page - 5)); // 시작 페이지 번호
		model.addAttribute("endPage", Math.min(page + 4, membersPage.getTotalPages() - 1)); // 종료 페이지 번호

		return "contents/admin/memberList"; // 관리자용 회원 정보 페이지
	}

}