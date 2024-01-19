package noninoni.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import noninoni.dto.OrderInfoDTO;
import noninoni.entity.Order;
import noninoni.repository.OrderInfoRepository;
import noninoni.repository.OrderRepository;
import noninoni.service.ProductVariantService;

@Component
public class OrderScheduler {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductVariantService productVariantService;
	
	@Autowired
	private OrderInfoRepository orderInfoRepository;

	// 매일 15분마다 실행
	@Scheduled(cron = "0 */15 * * * *")
	@Transactional
	public void checkAndCancelTemporaryOrders() {
	    // 현재 시간 기준으로 유효 시간이 지난 임시 주문 찾기
	    LocalDateTime now = LocalDateTime.now();
	    List<Order> expiredOrders = orderRepository.findByStatusAndPaidAtBefore("TEMPORARY", now.minusMinutes(15));

	    for (Order order : expiredOrders) {
	        // 재고 복원을 위한 OrderInfo 정보 추출
	        List<OrderInfoDTO> orderInfos = order.getOrderInfos().stream()
	                .map(oi -> new OrderInfoDTO(oi.getProductId(), oi.getVariant().getId(), oi.getQuantity()))
	                .collect(Collectors.toList());

	        // 재고 복원
	        productVariantService.restoreStock(orderInfos);

	        // 관련된 OrderInfo 삭제
	        orderInfoRepository.deleteAll(order.getOrderInfos());

	        //주문 삭제
	        orderRepository.delete(order); // 주문 삭제
	    }
	}

}