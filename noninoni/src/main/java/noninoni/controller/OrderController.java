package noninoni.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import noninoni.dto.CustomUserDetails;
import noninoni.entity.Member;
import noninoni.entity.Order;
import noninoni.entity.OrderInfo;
import noninoni.entity.OrderStatus;
import noninoni.service.OrderInfoService;
import noninoni.service.OrderService;
import noninoni.service.TrackingService;

@Slf4j
@Controller
public class OrderController {
	private final OrderService orderService;

	@Autowired
	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}
	@Autowired
	private OrderInfoService orderInfoService;

	@Autowired
	private TrackingService trackingService;

	@PostMapping("/admin/updateOrderStatus")
	public String updateOrderStatus(@RequestParam Long orderInfoId, @RequestParam String trackNumber,
			@RequestParam String orderStatus) {
		OrderInfo orderInfo = orderInfoService.findById(orderInfoId);
		if (orderInfo != null) {
			if (trackNumber.equals("상태변경") || trackNumber == null) {
				orderInfo.setTrackNumber(orderInfo.getTrackNumber() + "+");
			} else {
				orderInfo.setTrackNumber(trackNumber);
			}

			orderInfo.setOrderStatus(OrderStatus.valueOf(orderStatus));
			orderInfo.getOrder().setCancellable(false);
			orderInfo.setOrderConfirm(false);
			orderInfoService.save(orderInfo);

			// 추가적인 로직 (예: 주문 상태 변경에 따른 알림 등)
		} else {
			// 에러 처리
		}
		return "redirect:/admin/orders";
	}

	@GetMapping("/mypage")
	public String showMyPage(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String memberId = userDetails.getUsername();

			List<Order> orders = orderService.getAllOrdersByMemberId(memberId);
			String AccessToken = fetchAccessToken();

			// 각 주문에 대한 배송 상태 업데이트
			for (Order order : orders) {
				List<OrderInfo> orderInfos = order.getOrderInfos();
				trackingService.checkAndUpdateDeliveryStatus(orderInfos, AccessToken);

			}
			// OrderStatus의 모든 상태에 대해 반복 처리
			for (OrderStatus status : OrderStatus.values()) {
				String statusName = status.toString();
				int count = orderService.getCountByMemberIdAndStatus(memberId, status);
				model.addAttribute(statusName + "_count", count);
			}

			BigDecimal totalAmount = orderService.calculateTotalAmountByMemberId(memberId);
			int orderCount = orderService.countByMemberId(memberId);
			model.addAttribute("totalAmount", totalAmount);
			model.addAttribute("orderCount", orderCount);
		}

		return "contents/mypage";
	}

	@GetMapping("/admin/orders")
	public String adminlistOrders(@RequestParam(required = false) String period,
			@RequestParam(required = false) String orderStatus,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) String keyword, // 키워드 매개변수 추가
			Model model) {

		LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
		LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
		OrderStatus status = (orderStatus != null && !orderStatus.isEmpty()) ? OrderStatus.valueOf(orderStatus) : null;

		List<Order> orders;

		if (keyword != null && !keyword.isEmpty()) {
			// 키워드 검색 로직을 사용하여 주문 검색
			orders = orderService.searchOrders(keyword, status, startDateTime, endDateTime, period);
		} else {
			// 기존 로직을 유지
			if (period != null && status == null) {

				orders = orderService.getOrdersPeriod(period);
			} else if (startDate != null && endDate != null && status == null) {
				orders = orderService.getOrdersDates(startDateTime, endDateTime);
			} else if (status != null) {
				if (period != null) {
					orders = orderService.getOrdersPeriodAndStatus(period, status);
				} else if (startDate != null && endDate != null) {
					orders = orderService.getOrdersByDatesAndStatus(startDateTime, endDateTime, status);
				} else {
					orders = orderService.getOrdersStatus(status);
				}
			} else {

				period = "today";
				orders = orderService.getOrdersPeriod(period);
			}
		}
		String AccessToken = fetchAccessToken();

		// 각 주문에 대한 배송 상태 업데이트
		for (Order order : orders) {
			List<OrderInfo> orderInfos = order.getOrderInfos();
			trackingService.checkAndUpdateDeliveryStatus(orderInfos, AccessToken);
		}
		model.addAttribute("AccessToken", AccessToken);
		model.addAttribute("orderStatus", orderStatus);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("period", period);
		model.addAttribute("keyword", keyword); // 모델에 키워드 추가
		model.addAttribute("orders", orders);

		return "contents/admin/ordermanagement";
	}

	@GetMapping("/orders")
	public String listOrders(@RequestParam(required = false) String period,
			@RequestParam(required = false) String orderStatus,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) String keyword, Model model) {

		LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
		LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String memberId = userDetails.getUsername();

			List<Order> orders = orderService.getAllOrdersByMemberId(memberId);
			String AccessToken = fetchAccessToken();

			// 각 주문에 대한 배송 상태 업데이트
			for (Order order : orders) {
				List<OrderInfo> orderInfos = order.getOrderInfos();
				trackingService.checkAndUpdateDeliveryStatus(orderInfos, AccessToken);
			}

			OrderStatus status = (orderStatus != null && !orderStatus.isEmpty()) ? OrderStatus.valueOf(orderStatus)
					: null;
			period = (period != null && !period.isEmpty()) ? period : null;

			if (keyword != null && !keyword.isEmpty()) {
				// 키워드 검색 로직을 사용하여 주문 검색
				orders = orderService.searchOrdersByMemberId(memberId, keyword, status, startDateTime, endDateTime,
						period);
			} else {
				if (period != null && status == null) {
					orders = orderService.getOrdersByMemberIdAndPeriod(memberId, period);
				} else if (startDate != null && endDate != null && status == null) {
					orders = orderService.getOrdersByMemberIdAndDates(memberId, startDateTime, endDateTime);
				} else if (status != null) {
					if (period != null) {
						orders = orderService.getOrdersByMemberIdPeriodAndStatus(memberId, period, status);
					} else if (startDate != null && endDate != null) {
						orders = orderService.getOrdersByMemberIdDatesAndStatus(memberId, startDateTime, endDateTime,
								status);
					} else {
						orders = orderService.getOrdersByMemberIdAndStatus(memberId, status);
					}
				} else {
					period = "today";
					orders = orderService.getOrdersByMemberIdAndPeriod(memberId, period);
				}
			}

			model.addAttribute("AccessToken", AccessToken);
			model.addAttribute("orderStatus", orderStatus);
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);
			model.addAttribute("period", period);
			model.addAttribute("orders", orders);
			model.addAttribute("keyword", keyword);
		}

		return "contents/orderlist";
	}

	@Autowired
	private RestTemplate restTemplate;

	private static final long TOKEN_EXPIRATION_TIME = 3600 * 1000; // 토큰 유효 시간, 예: 1시간

	private String currentAccessToken;
	private long accessTokenExpirationTime;

	private String fetchAccessToken() {
		// 현재 시간
		long now = System.currentTimeMillis();

		// 토큰이 유효한 경우 재사용
		if (currentAccessToken != null && now < accessTokenExpirationTime) {
			return currentAccessToken;
		}

		// 새로운 토큰 요청
		try {
			String tokenUrl = "https://auth.tracker.delivery/oauth2/token";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("grant_type", "client_credentials");
			map.add("client_id", "1j2ah7ebb86ba67nmrebv8rce4");
			map.add("client_secret", "1f7ueannfjf4ne9rnuq2q5kh04kbk3o2fu9k20gcrjrfh2kgvrst");

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

			// JSON 응답 파싱
			JSONObject jsonObject = new JSONObject(response.getBody());
			currentAccessToken = jsonObject.getString("access_token");
			accessTokenExpirationTime = now + TOKEN_EXPIRATION_TIME;

			return currentAccessToken;
		} catch (Exception e) {
			e.printStackTrace();
			return null; // 오류 발생 시 null 반환
		}
	}

	// 주문 상세 페이지로 이동
	@GetMapping("/orders/detail/{ordernum}")
	public String orderdetail(@PathVariable("ordernum") Long id, Model model, HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String memberId = userDetails.getUsername();
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			boolean isAdmin = false;

			if (principal instanceof UserDetails) {
				// 관리자 권한을 확인합니다. (관리자 권한이 'ROLE_ADMIN'이라고 가정)
				isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
			}
			// 특정 회원의 특정 주문을 가져옵니다.
			Order order = orderService.getOrderByIdAndMemberId(id, memberId, isAdmin);
			String AccessToken = fetchAccessToken();

			// 주문에 대한 배송 상태 업데이트
			List<OrderInfo> orderInfos = order.getOrderInfos();
			trackingService.checkAndUpdateDeliveryStatus(orderInfos, AccessToken);
			BigDecimal totalPrice = calculateTotalPrice(orderInfos);/* totalPrice를 계산하는 로직 */
			if (totalPrice == null) {
				totalPrice = BigDecimal.ZERO;
			}
			model.addAttribute("totalPrice", totalPrice);
			BigDecimal shippingCost = order.getShippingCost();
			if (shippingCost == null) {
				shippingCost = BigDecimal.ZERO;
			}

			BigDecimal finalPrice = totalPrice.add(shippingCost);

			Member member = order.getMember();
			model.addAttribute("member", member);
			model.addAttribute("finalPrice", finalPrice);
			model.addAttribute("shippingCost", shippingCost);
			// 모델에 주문 데이터 추가
			model.addAttribute("order", order);
			model.addAttribute("AccessToken", AccessToken);

		}
		// 주문 상세 정보를 보여줄 뷰의 이름 반환
		return "contents/orderdetail"; // 'orderDetail'은 주문 상세 정보를 보여주는 HTML 템플릿 파일의 이름입니다.
	}

	// 총액 계산 메소드
	public BigDecimal calculateTotalPrice(List<OrderInfo> orderInfos) {
		return orderInfos.stream().map(OrderInfo::getSumPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}