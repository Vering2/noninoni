package noninoni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import noninoni.entity.OrderInfo;

@Repository
public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long> {
	// 주문 ID로 주문 항목을 조회하는 메소드 예시
	List<OrderInfo> findByOrderId(Long orderId);

	List<OrderInfo> findByTrackNumber(String trackNumber);

	OrderInfo findByReviewId(Long reviewId);

	List<OrderInfo> findByProductNameContaining(String searchTerm);

	List<OrderInfo> findByProductId(Long id);
}
