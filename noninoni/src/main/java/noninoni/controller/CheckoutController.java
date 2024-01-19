package noninoni.controller;

import noninoni.dto.CartItemDto;
import noninoni.dto.CustomUserDetails;
import noninoni.dto.DeliveryAddressDTO;
import noninoni.entity.DeliveryAddress;
import noninoni.entity.DeliveryFee;
import noninoni.entity.Member;
import noninoni.repository.DeliveryFeeRepository;
import noninoni.service.CartService;
import noninoni.service.DeliveryAddressService;
import noninoni.service.MemberService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/checkout")
public class CheckoutController {
	@Autowired
	private CartService cartService;


	@Autowired
	private MemberService memberService; // MemberService 또는 적절한 서비스를 주입

	@Autowired
	private DeliveryAddressService deliveryAddressService;

	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	// 비동기 처리를 위한 ExecutorService
	@Autowired
	private ExecutorService executorService; // ExecutorService를 Bean으로 등록하고 주입

	@Autowired
	private DeliveryFeeRepository deliveryFeeRepository;

	@GetMapping
	public String showCheckoutPage(@RequestParam("products") String productIds,
			@RequestParam(value = "colors", required = false) String colorIds,
			@RequestParam(value = "sizes", required = false) String sizeIds, Model model, HttpServletRequest request,
			Authentication authentication) throws Exception {

		Long cartId = (Long) request.getSession().getAttribute("cartId");
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

		CompletableFuture<List<CartItemDto>> cartItemsFuture = CompletableFuture
				.supplyAsync(() -> cartService.getCartItemsWithProductVariantDetails(cartId, parseIds(productIds),
						parseIds(colorIds), parseIds(sizeIds)), executorService);

		// 모든 비동기 작업이 완료될 때까지 기다립니다.
		CompletableFuture.allOf(addressesFuture, defaultAddressFuture, cartItemsFuture).join();

		List<DeliveryAddress> addresses = addressesFuture.get();
		DeliveryAddress defaultAddress = defaultAddressFuture.get();
		List<CartItemDto> cartItems = cartItemsFuture.get();

		// 모델에 데이터 추가
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("deliveryAddresses", addresses);
		model.addAttribute("defaultaddress", defaultAddress);
		long feeId = 1;
		DeliveryFee deliveryFee = deliveryFeeRepository.getReferenceById(feeId);
		long totalPrice = calculateTotalPrice(cartItems);
		long shippingCost = calculateShippingCost(totalPrice, deliveryFee.getFee().longValue(),
				deliveryFee.getMinimum().longValue());
		long finalPrice = totalPrice + shippingCost;

		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("shippingCost", shippingCost);
		model.addAttribute("finalPrice", finalPrice);
		model.addAttribute("member", memberService.findMemberById(memberId));

		return "contents/checkout";
	}

	private String getMemberIdFromAuthentication(Authentication authentication) {
		return authentication != null && authentication.getPrincipal() instanceof CustomUserDetails
				? ((CustomUserDetails) authentication.getPrincipal()).getUsername()
				: null;
	}

	private List<Long> parseIds(String idString) {
		return idString != null ? Arrays.stream(idString.split(",")).map(Long::parseLong).collect(Collectors.toList())
				: new ArrayList<>();
	}

	private long calculateTotalPrice(List<CartItemDto> cartItems) {
		return cartItems.stream().mapToLong(item -> (item.getProduct().getPrice().longValue() + item.getVariant().getAdditionalPrice())  * item.getQuantity())
				.sum();
	}

	private long calculateShippingCost(long totalPrice, long shippingCost, long freeShippingThreshold) {
		return totalPrice >= freeShippingThreshold ? 0L : shippingCost;
	}

	@PostMapping("/saveDeliveryAddress")
	public ResponseEntity<?> saveDeliveryAddress(@RequestBody DeliveryAddressDTO deliveryAddressDTO,
			Authentication authentication) {

		DeliveryAddress deliveryAddress = deliveryAddressDTO.getDeliveryAddress();
		String[] phoneNumber = deliveryAddressDTO.getPhoneNumber();
		String[] address = deliveryAddressDTO.getAddress();


		// 주소 설정
		if (address != null && address.length == 2) {
			deliveryAddress.setAddress(address[0] + " " + address[1]);
		}

		// 전화번호 설정
		if (phoneNumber != null && phoneNumber.length == 3) {
			deliveryAddress.setPhoneNumber(phoneNumber[0] + "-" + phoneNumber[1] + "-" + phoneNumber[2]);
		}

		String memberId = null;

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			memberId = userDetails.getUsername();
		}

		if (memberId == null) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("저장 중 오류 발생");
		} else {
			deliveryAddressService.saveDeliveryAddress(deliveryAddress, memberId);
			return ResponseEntity.ok().body("Delivery address saved successfully");
		}
	}

	@PostMapping("/setDefaultAddress")
	public ResponseEntity<?> setDefaultAddress(@RequestBody Map<String, Long> payload, Authentication authentication) {
		Long addressId = payload.get("addressId");
		String memberId = null;
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			memberId = userDetails.getUsername();
		}
		// UserService 등을 통해 사용자의 기본 배송지 설정
		boolean isSuccess = memberService.setDefaultAddressForMember(memberId, addressId);

		if (isSuccess) {
			return ResponseEntity.ok().body("기본 배송지가 성공적으로 설정되었습니다.");
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("기본 배송지 설정 실패");
		}
	}

	@PostMapping("/deleteDeliveryAddress")
	public ResponseEntity<?> deleteDeliveryAddress(@RequestParam Long addressId, Authentication authentication) {
		try {
			String memberId = null;
			if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
				CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
				memberId = userDetails.getUsername();
				deliveryAddressService.deleteAddress(memberId,addressId);
			}				
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 중 오류 발생");
		}
	}

}
