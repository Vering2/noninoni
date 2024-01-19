package noninoni.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import noninoni.dto.OrderInfoDTO;
import noninoni.dto.PaymentData;
import noninoni.dto.verifyData;
import noninoni.entity.Order;
import noninoni.entity.OrderInfo;
import noninoni.entity.OrderStatus;
import noninoni.entity.ProductVariant;
import noninoni.repository.OrderRepository;
import noninoni.service.CartService;
import noninoni.service.OrderService;
import noninoni.service.ProductService;
import noninoni.service.ProductVariantService;

@Slf4j
@Controller
public class ImportApiController {

	@Autowired
	private ProductService productService;

	@Autowired
	private CartService cartService;

	private IamportClient iamportClient;

	@Autowired
	private OrderService orderService; // MemberService 주입

	@Autowired
	private OrderRepository orderRepository; // OrderRepository를 주입받습니다.

	@GetMapping("/payment/mobile/success")
	public String orderCompleteMobile(@RequestParam(required = false) String imp_uid,
			@RequestParam(required = false) String merchant_uid, Model model, Locale locale, HttpSession session)
			throws IamportResponseException, IOException {

		IamportResponse<Payment> paymentResponse = iamportClient.paymentByImpUid(imp_uid);

		// 실제 결제 금액 확인
		BigDecimal actualAmount = paymentResponse.getResponse().getAmount();
		Order order = orderRepository.findByMerchantUid(paymentResponse.getResponse().getMerchantUid())
				.orElseThrow(() -> new RuntimeException("주문을 찾을 수 없음"));
		BigDecimal expectedAmount = calculateTotalAmount(order.getOrderInfos()).add(order.getShippingCost());
		// 상품 정보를 통한 총액 계산
		boolean isValid = actualAmount.compareTo(expectedAmount) == 0;

		if (isValid) {
			order.setImpUid(paymentResponse.getResponse().getImpUid());
			order.setPayMethod(paymentResponse.getResponse().getPayMethod());
			order.setMerchantUid(paymentResponse.getResponse().getMerchantUid());
			order.setPaidAmount(actualAmount);
			order.setApplyNum(paymentResponse.getResponse().getApplyNum());
			order.setStatus(paymentResponse.getResponse().getStatus());
			Date date = paymentResponse.getResponse().getPaidAt(); // Date 타입
			LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			order.setPaidAt(localDateTime);
			order.setBuyerEmail(paymentResponse.getResponse().getBuyerEmail());
			order.setReceiptUrl(paymentResponse.getResponse().getReceiptUrl());
			order.setBankName(paymentResponse.getResponse().getBankName());
			order.setCardName(paymentResponse.getResponse().getCardName());
			order.setCardNumber(paymentResponse.getResponse().getCardNumber());
			order.getOrderInfos().forEach(orderInfo -> {
				orderInfo.setOrderStatus(OrderStatus.PREPARING_SHIPMENT); // 주문 상태를 '배송 준비중'으로 설정
				// 필요한 다른 속성들도 여기에서 업데이트
			});
			orderRepository.save(order); // 주문 정보를 데이터베이스에 저장
			// 결제가 성공한 사용자의 장바구니 아이템을 삭제
			cartService.clearCartItems(order.getMember().getMemberId(), order.getOrderInfos());
			return "redirect:/orders/detail/" + order.getId();
		} else {
			orderService.refundRequest(iamportClient.getAuth().getResponse().getToken(),
					paymentResponse.getResponse().getImpUid(), "결제 오류로 인한 취소", actualAmount, order.getId(),
					actualAmount);
			return "redirect:/cart";
		}

	}

	public ImportApiController() {
		// REST API 키와 REST API secret 를 아래처럼 순서대로 입력한다.
		this.iamportClient = new IamportClient(System.getenv("IamportKey"),
				System.getenv("IamportSeretkey"));
	}

	@PostMapping("/cancel")
	public ResponseEntity<?> cancelPayment(@RequestBody Order request) {
		try {
			String resultMessage = orderService.refundRequest(iamportClient.getAuth().getResponse().getToken(),
					request.getImpUid(), request.getReason(), request.getPaidAmount(), request.getId(),
					request.getCancelAmount());

			return ResponseEntity.ok(resultMessage); // 성공 메시지를 반환
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 오류 메시지를 반환
		}
	}

	@Autowired
	private ProductVariantService productVariantService;

	@PostMapping("/prepare-order")
	public ResponseEntity<?> prepareOrder(@RequestBody PaymentData paymentData) {
		List<OrderInfoDTO> orderInfoDTOs = paymentData.getOrderInfo();

		if (productVariantService.checkAndDecreaseStock(orderInfoDTOs)) {
			try {
				Order temporaryOrder = orderService.createTemporaryOrder(paymentData);
				return ResponseEntity.ok(temporaryOrder.getId());
			} catch (Exception e) {
				// 임시 주문 생성에 실패한 경우, 재고를 다시 복원하는 로직을 여기에 추가
				productVariantService.restoreStock(orderInfoDTOs);
				return ResponseEntity.badRequest().body("Failed to create temporary order: " + e.getMessage());
			}
		} else {
			Map<String, Integer> availableQuantities = productVariantService.getAvailableQuantities(orderInfoDTOs);
			return ResponseEntity.badRequest().body(availableQuantities);

		}
	}

	@PostMapping("/verifyIamport")
	public ResponseEntity<?> verifyPayment(@RequestBody verifyData verifyData)
			throws IamportResponseException, IOException {

		// 아임포트 API를 통해 결제 정보 요청
		IamportResponse<Payment> paymentResponse = iamportClient.paymentByImpUid(verifyData.getImpUid());

		// 실제 결제 금액 확인
		BigDecimal actualAmount = paymentResponse.getResponse().getAmount();
		Order order = orderRepository.findByMerchantUid(paymentResponse.getResponse().getMerchantUid())
				.orElseThrow(() -> new RuntimeException("주문을 찾을 수 없음"));
		BigDecimal expectedAmount = calculateTotalAmount(order.getOrderInfos()).add(order.getShippingCost());
		// 상품 정보를 통한 총액 계산
		boolean isValid = actualAmount.compareTo(expectedAmount) == 0;
		if (isValid) {
			order.setImpUid(paymentResponse.getResponse().getImpUid());
			order.setPayMethod(paymentResponse.getResponse().getPayMethod());
			order.setMerchantUid(paymentResponse.getResponse().getMerchantUid());
			order.setPaidAmount(actualAmount);
			order.setApplyNum(paymentResponse.getResponse().getApplyNum());
			order.setStatus(paymentResponse.getResponse().getStatus());
			Date date = paymentResponse.getResponse().getPaidAt(); // Date 타입
			LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			order.setPaidAt(localDateTime);
			order.setBuyerEmail(paymentResponse.getResponse().getBuyerEmail());
			order.setReceiptUrl(paymentResponse.getResponse().getReceiptUrl());
			order.setBankName(paymentResponse.getResponse().getBankName());
			order.setCardName(paymentResponse.getResponse().getCardName());
			order.setCardNumber(paymentResponse.getResponse().getCardNumber());

			order.getOrderInfos().forEach(orderInfo -> {
				orderInfo.setOrderStatus(OrderStatus.PREPARING_SHIPMENT); // 주문 상태를 '배송 준비중'으로 설정
				// 필요한 다른 속성들도 여기에서 업데이트
			});
			orderRepository.save(order); // 주문 정보를 데이터베이스에 저장

			// 결제가 성공한 사용자의 장바구니 아이템을 삭제
			cartService.clearCartItems(order.getMember().getMemberId(), order.getOrderInfos());

		}
		// 결과 반환
		Map<String, Boolean> response = new HashMap<>();
		response.put("isValid", isValid);

		return ResponseEntity.ok(response);
	}

	public BigDecimal calculateTotalAmount(List<OrderInfo> orderInfos) {
		BigDecimal totalAmount = BigDecimal.ZERO;

		for (OrderInfo orderInfo : orderInfos) {
			BigDecimal priceFromDb = productService.getPriceById(orderInfo.getProductId()); // DB에서 상품 가격 조회
			ProductVariant productVariant = productVariantService.getProductVariant(orderInfo.getVariant().getId());
			int additionalPriceFromDbInt = productVariant.getAdditionalPrice(); // int 형식의 additionalPrice
			BigDecimal additionalPriceFromDb = BigDecimal.valueOf(additionalPriceFromDbInt); // int를 BigDecimal로 변환

			BigDecimal priceFromOrder = orderInfo.getPrice(); // OrderInfo 객체의 가격
			BigDecimal totalPrice = priceFromDb.add(additionalPriceFromDb);

			if (totalPrice.compareTo(priceFromOrder) != 0) {
				throw new RuntimeException("가격 불일치: " + orderInfo.getProductId());
			}

			Integer quantity = orderInfo.getQuantity(); // 수량
			totalAmount = totalAmount.add(totalPrice.multiply(new BigDecimal(quantity)));
		}

		return totalAmount;
	}

}