package noninoni.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import noninoni.dto.OrderInfoDTO;
import noninoni.dto.PaymentData;
import noninoni.entity.Member;
import noninoni.entity.Order;
import noninoni.entity.OrderInfo;
import noninoni.entity.OrderStatus;
import noninoni.repository.OrderRepository;
import noninoni.repository.ProductVariantRepository;

@Slf4j
@Service
public class OrderService {
	private final OrderRepository orderRepository;

	@Autowired
	private ProductVariantRepository productVariantRepository;
	
	@Autowired
	private MemberService memberService; // MemberService 주입

	@Autowired
	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}


	public List<Order> getAllOrdersByMemberId(String memberId) {
		// OrderRepository의 findByUserId 메소드를 호출하여 사용자 ID로 주문을 조회
		return orderRepository.findByMemberMemberIdOrderByPaidAtDesc(memberId);
	}

	public List<Order> getOrdersByMemberIdPeriodAndStatus(String memberId, String period, OrderStatus status) {
		// 여기에 period에 따른 날짜 계산 로직 추가
		// ...
		LocalDateTime startDateTime = LocalDate.now().atStartOfDay(); // 하루의 시작 시간
		LocalDateTime endDateTime = LocalDate.now().atTime(LocalTime.MAX); // 하루의 종료 시간

		switch (period) {
		case "today":
			return orderRepository.findByMemberMemberIdAndOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(
					memberId, status, startDateTime, endDateTime);
		case "1weeks":
			return orderRepository.findByMemberMemberIdAndOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(
					memberId, status, startDateTime.minusWeeks(1), endDateTime);
		case "1month":
			return orderRepository.findByMemberMemberIdAndOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(
					memberId, status, startDateTime.minusMonths(1), endDateTime);
		case "3months":
			return orderRepository.findByMemberMemberIdAndOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(
					memberId, status, startDateTime.minusMonths(3), endDateTime);
		case "6months":
			return orderRepository.findByMemberMemberIdAndOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(
					memberId, status, startDateTime.minusMonths(6), endDateTime);
		default:
			return new ArrayList<>();
		}
	}

	public List<Order> getOrdersByMemberIdDatesAndStatus(String memberId, LocalDateTime startDateTime,
			LocalDateTime endDateTime, OrderStatus status) {
		return orderRepository.findByMemberMemberIdAndOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(memberId,
				status, startDateTime, endDateTime);
	}

	public List<Order> getOrdersByMemberIdAndPeriod(String memberId, String period) {
		LocalDateTime startDateTime;
		LocalDateTime endDateTime = LocalDate.now().atTime(LocalTime.MAX);

		switch (period) {
		case "today":
			startDateTime = LocalDate.now().atStartOfDay();
			break;
		case "1weeks":
			startDateTime = LocalDate.now().minusWeeks(1).atStartOfDay();
			break;
		case "1month":
			startDateTime = LocalDate.now().minusMonths(1).atStartOfDay();
			break;
		case "3months":
			startDateTime = LocalDate.now().minusMonths(3).atStartOfDay();
			break;
		case "6months":
			startDateTime = LocalDate.now().minusMonths(6).atStartOfDay();
			break;
		default:
			return new ArrayList<>();
		}

		return orderRepository.findByMemberMemberIdAndPaidAtBetweenOrderByPaidAtDesc(memberId, startDateTime,
				endDateTime);
	}

	public List<Order> getOrdersByMemberIdAndDates(String memberId, LocalDateTime startDateTime,
			LocalDateTime endDateTime) {
		return orderRepository.findByMemberMemberIdAndPaidAtBetweenOrderByPaidAtDesc(memberId, startDateTime,
				endDateTime);
	}

	public List<Order> getOrdersByMemberIdAndStatus(String memberId, OrderStatus status) {
		return orderRepository.findByMemberMemberIdAndOrderInfosOrderStatusOrderByPaidAtDesc(memberId, status);
	}

	public Order getOrderByIdAndMemberId(Long id, String memberId, boolean isAdmin) {
		if (isAdmin) {
			// 관리자의 경우, id에 해당하는 주문을 가져옵니다.
			return orderRepository.findById(id).orElse(null);
		} else {
			// 일반 사용자의 경우, memberId와 id에 해당하는 주문을 가져옵니다.
			return orderRepository.findByIdAndMemberMemberId(id, memberId).orElse(null);
		}
	}

	public int getCountByMemberIdAndStatus(String memberId, OrderStatus status) {
		// TODO Auto-generated method stub
		LocalDateTime startDateTime;
		LocalDateTime endDateTime = LocalDate.now().atTime(LocalTime.MAX);
		startDateTime = LocalDate.now().minusMonths(3).atStartOfDay();
		return orderRepository.countByMemberMemberIdAndOrderInfosOrderStatusAndPaidAtBetween(memberId, status,
				startDateTime, endDateTime);
	}

	public BigDecimal calculateTotalAmountByMemberId(String memberId) {
		// TODO Auto-generated method stub
		LocalDateTime startDateTime;
		LocalDateTime endDateTime = LocalDate.now().atTime(LocalTime.MAX);
		startDateTime = LocalDate.now().minusMonths(3).atStartOfDay();

		// 주문 상태가 "paid" 인 주문의 총 결제 금액 계산
		List<Order> paidOrders = orderRepository.findByMemberMemberIdAndStatusAndPaidAtBetween(memberId, "paid",
				startDateTime, endDateTime);
		BigDecimal totalPaidAmount = BigDecimal.ZERO;
		for (Order order : paidOrders) {
			totalPaidAmount = totalPaidAmount.add(order.getPaidAmount());
		}

		return totalPaidAmount;
	}

	public int countByMemberId(String memberId) {
		// TODO Auto-generated method stub
		LocalDateTime startDateTime;
		LocalDateTime endDateTime = LocalDate.now().atTime(LocalTime.MAX);
		startDateTime = LocalDate.now().minusMonths(3).atStartOfDay();
		return orderRepository.countByMemberMemberIdAndPaidAtBetween(memberId, startDateTime, endDateTime);
	}

	@Transactional
	public Order createTemporaryOrder(PaymentData paymentData) {
		List<OrderInfoDTO> orderInfoDTOs = paymentData.getOrderInfo();
		Order temporaryOrder = new Order();
		temporaryOrder.setStatus("TEMPORARY");
		Long OrderId = paymentData.getOrderId();
		List<OrderInfo> orderInfos = orderInfoDTOs.stream().map(dto -> {
			OrderInfo orderInfo = new OrderInfo();
			orderInfo.setVariant(productVariantRepository.findById(dto.getVariantId()).orElse(null));
			orderInfo.setQuantity(dto.getQuantity());
			orderInfo.setPrice(dto.getPrice());
			orderInfo.setOrderStatus(OrderStatus.TEMPORARY);
			orderInfo.setSumPrice(dto.calculateSumPrice());
			orderInfo.setOrder(temporaryOrder);
			orderInfo.setProductId(dto.getProductId());
			orderInfo.setProductName(dto.getProductName());
			orderInfo.setSumPrice(dto.calculateSumPrice());
			return orderInfo;
		}).collect(Collectors.toList());
		temporaryOrder.setId(OrderId);
		temporaryOrder.setMerchantUid(paymentData.getMerchantUid());
		temporaryOrder.setOrderInfos(orderInfos);
		Member member = memberService.findMemberById(paymentData.getMemberId());
		temporaryOrder.setMember(member);
		temporaryOrder.setRecipient(paymentData.getRecipient());
		temporaryOrder.setRecphoneNumber(paymentData.getRecphoneNumber());
		temporaryOrder.setRecaddress(paymentData.getRecaddress());
		temporaryOrder.setOmessage(paymentData.getOmessage());
		temporaryOrder.setName(paymentData.getProductNames());
		temporaryOrder.setShippingCost(paymentData.getShippingCost());

		orderRepository.save(temporaryOrder);
		return temporaryOrder;
	}

	public String refundRequest(String iamportResponse, String impUid, String reason, BigDecimal amount, long Id,
			BigDecimal checksum) throws IOException {
		HttpsURLConnection conn = null;
		URL url = new URL("https://api.iamport.kr/payments/cancel");

		conn = (HttpsURLConnection) url.openConnection();

		conn.setRequestMethod("POST");

		conn.setRequestProperty("Content-type", "application/json");
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Authorization", iamportResponse);

		conn.setDoOutput(true);

		JsonObject json = new JsonObject();

		json.addProperty("reason", reason);
		impUid = impUid.replace("\"", "");
		json.addProperty("imp_uid", impUid);
		json.addProperty("amount", amount);
		json.addProperty("checksum", checksum);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));

		bw.write(json.toString());
		bw.flush();
		bw.close();

		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));

		String responseJson;
		responseJson = br.lines().collect(Collectors.joining("\n"));


		JsonObject jsonResponse = JsonParser.parseString(responseJson).getAsJsonObject();
		String resultCode = jsonResponse.get("code").getAsString();
		String resultMessage = jsonResponse.get("message").getAsString();


		conn.disconnect();
		// 주문 취소 처리
		if (resultCode.equals("1")) {
			cancelOrder(Id, amount, reason, checksum);
		}
		return resultMessage;

	}

	@Autowired
	private ProductVariantService productVariantService;

	public void cancelOrder(Long orderId, BigDecimal CancelAmount, String reason, BigDecimal checksum) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다."));
		BigDecimal currentCancelAmount = order.getCancelAmount() != null ? order.getCancelAmount() : BigDecimal.ZERO;
		// 두 BigDecimal 값을 더하기
		BigDecimal totalCancelAmount = currentCancelAmount.add(CancelAmount);
		String totalreason = order.getReason() + "이유:" + reason + "금액:" + CancelAmount;
		if (checksum.compareTo(CancelAmount) == 0) {

			// 재고 복원을 위한 OrderInfo 정보 추출 및 재고 복원
			List<OrderInfoDTO> orderInfos = order.getOrderInfos().stream().map(oi -> {
				oi.setOrderStatus(OrderStatus.CANCELED); // 주문 상태를 CANCELED로 업데이트
				return new OrderInfoDTO(oi.getProductId(), oi.getVariant().getId(), oi.getQuantity());
			}).collect(Collectors.toList());

			productVariantService.restoreStock(orderInfos);
			// order.getCancelAmount()이 null이면 BigDecimal.ZERO를 사용, 아니면 해당 값을 사용
			order.setStatus("cancel"); // 주문 상태 업데이트
		} else {
			// 주문 정보 업데이트

			order.setStatus("refund");

		}
		order.setCancelAmount(totalCancelAmount);
		order.setReason(totalreason);
		order.setCancellable(false);
		orderRepository.save(order);
	}

	public List<Order> getAll() {
		return orderRepository.findAll(); // 모든 주문 조회 및 반환
	}

	public List<Order> getOrdersPeriod(String period) {
		LocalDateTime startDateTime;
		LocalDateTime endDateTime = LocalDateTime.now(); // 현재 시간까지의 주문 조회

		switch (period) {
		case "today":
			startDateTime = LocalDate.now().atStartOfDay();
			break;
		case "1weeks":
			startDateTime = LocalDate.now().minusWeeks(1).atStartOfDay();
			break;
		case "1month":
			startDateTime = LocalDate.now().minusMonths(1).atStartOfDay();
			break;
		case "3months":
			startDateTime = LocalDate.now().minusMonths(3).atStartOfDay();
			break;
		case "6months":
			startDateTime = LocalDate.now().minusMonths(6).atStartOfDay();
			break;
		default:
			return new ArrayList<>();
		}

		return orderRepository.findByPaidAtBetweenOrderByPaidAtDesc(startDateTime, endDateTime);
	}

	public List<Order> getOrdersDates(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		// TODO Auto-generated method stub
		return orderRepository.findByPaidAtBetweenOrderByPaidAtDesc(startDateTime, endDateTime);
	}

	public List<Order> getOrdersPeriodAndStatus(String period, OrderStatus status) {
		// 여기에 period에 따른 날짜 계산 로직 추가
		// ...
		LocalDateTime startDateTime = LocalDate.now().atStartOfDay(); // 하루의 시작 시간
		LocalDateTime endDateTime = LocalDate.now().atTime(LocalTime.MAX); // 하루의 종료 시간

		switch (period) {
		case "today":
			return orderRepository.findByOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(status, startDateTime,
					endDateTime);
		case "1weeks":
			return orderRepository.findByOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(status,
					startDateTime.minusWeeks(1), endDateTime);
		case "1month":
			return orderRepository.findByOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(status,
					startDateTime.minusMonths(1), endDateTime);
		case "3months":
			return orderRepository.findByOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(status,
					startDateTime.minusMonths(3), endDateTime);
		case "6months":
			return orderRepository.findByOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(status,
					startDateTime.minusMonths(6), endDateTime);
		default:
			return new ArrayList<>();
		}
	}

	public List<Order> getOrdersByDatesAndStatus(LocalDateTime startDateTime, LocalDateTime endDateTime,
			OrderStatus status) {
		return orderRepository.findByOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(status, startDateTime,
				endDateTime);
	}

	public List<Order> getOrdersStatus(OrderStatus status) {
		// TODO Auto-generated method stub
		LocalDateTime startDateTime;
		LocalDateTime endDateTime = LocalDate.now().atTime(LocalTime.MAX);
		startDateTime = LocalDate.now().minusMonths(3).atStartOfDay();
		return orderRepository.countByOrderInfosOrderStatusAndPaidAtBetween(status, startDateTime, endDateTime);
	}

	@PersistenceContext
	private EntityManager em;

	public List<Order> searchOrders(String keyword, OrderStatus status, LocalDateTime startDateTime,
			LocalDateTime endDateTime, String period) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> order = cq.from(Order.class);
		List<Predicate> predicates = new ArrayList<>();

		// 키워드 기반 검색
		if (keyword != null && !keyword.isEmpty()) {
			try {
				long keywordAsLong = Long.parseLong(keyword);
				predicates.add(cb.equal(order.get("id"), keywordAsLong));
			} catch (NumberFormatException e) {
				Predicate keywordPredicate = cb.or(cb.like(order.get("name"), "%" + keyword + "%")
				// 추가적인 문자열 필드에 대한 조건을 추가할 수 있습니다.
				);
				predicates.add(keywordPredicate);
			}
		}

// 주문 상태 검색
		if (status != null) {
			predicates.add(cb.equal(order.get("status"), status));
		}

// 날짜 범위 및 기간 검색
		if (startDateTime != null && endDateTime != null) {
			predicates.add(cb.between(order.get("paidAt"), startDateTime, endDateTime));
		} else if (period != null && !period.isEmpty()) {
			startDateTime = LocalDate.now().atStartOfDay(); // 하루의 시작 시간
			endDateTime = LocalDate.now().atTime(LocalTime.MAX); // 하루의 종료 시간

			switch (period) {
			case "today":
// 오늘의 주문
				break;
			case "1weeks":
				startDateTime = startDateTime.minusWeeks(1);
				break;
			case "1month":
				startDateTime = startDateTime.minusMonths(1);
				break;
			case "3months":
				startDateTime = startDateTime.minusMonths(3);
				break;
			case "6months":
				startDateTime = startDateTime.minusMonths(6);
				break;
			default:
// 기본값 또는 예외 처리
				break;
			}
			predicates.add(cb.between(order.get("paidAt"), startDateTime, endDateTime));
		}

		cq.where(predicates.toArray(new Predicate[0]));
		TypedQuery<Order> query = em.createQuery(cq);
		return query.getResultList();
	}

	public List<Order> searchOrdersByMemberId(String memberId, String keyword, OrderStatus status,
			LocalDateTime startDateTime, LocalDateTime endDateTime, String period) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> order = cq.from(Order.class);
		List<Predicate> predicates = new ArrayList<>();

		// 멤버와 주문을 조인
		Join<Order, Member> memberJoin = order.join("member");

		// 멤버 ID 기반 검색
		if (memberId != null && !memberId.isEmpty()) {
			predicates.add(cb.equal(memberJoin.get("memberId"), memberId));
		}

		// 키워드 기반 검색
		if (keyword != null && !keyword.isEmpty()) {
			try {
				long keywordAsLong = Long.parseLong(keyword);
				predicates.add(cb.equal(order.get("id"), keywordAsLong));
			} catch (NumberFormatException e) {
				Predicate keywordPredicate = cb.or(cb.like(order.get("name"), "%" + keyword + "%")
				// 추가적인 문자열 필드에 대한 조건을 추가할 수 있습니다.
				);
				predicates.add(keywordPredicate);
			}
		}

// 주문 상태 검색
		if (status != null) {
			predicates.add(cb.equal(order.get("status"), status));
		}

// 날짜 범위 및 기간 검색
		if (startDateTime != null && endDateTime != null) {
			predicates.add(cb.between(order.get("paidAt"), startDateTime, endDateTime));
		} else if (period != null && !period.isEmpty()) {
			startDateTime = LocalDate.now().atStartOfDay(); // 하루의 시작 시간
			endDateTime = LocalDate.now().atTime(LocalTime.MAX); // 하루의 종료 시간

			switch (period) {
			case "today":
// 오늘의 주문
				break;
			case "1weeks":
				startDateTime = startDateTime.minusWeeks(1);
				break;
			case "1month":
				startDateTime = startDateTime.minusMonths(1);
				break;
			case "3months":
				startDateTime = startDateTime.minusMonths(3);
				break;
			case "6months":
				startDateTime = startDateTime.minusMonths(6);
				break;
			default:
// 기본값 또는 예외 처리
				break;
			}
			predicates.add(cb.between(order.get("paidAt"), startDateTime, endDateTime));
		}

		cq.where(predicates.toArray(new Predicate[0]));
		TypedQuery<Order> query = em.createQuery(cq);
		return query.getResultList();

	}

}