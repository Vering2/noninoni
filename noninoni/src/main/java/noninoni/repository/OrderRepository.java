package noninoni.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import noninoni.entity.Order;
import noninoni.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByMemberMemberIdOrderByPaidAtDesc(String memberId);

	List<Order> findByMemberMemberIdAndPaidAtBetweenOrderByPaidAtDesc(String memberId, LocalDateTime startDateTime,
			LocalDateTime endDateTime);

	List<Order> findByMemberMemberIdAndOrderInfosOrderStatusOrderByPaidAtDesc(String memberId, OrderStatus status);

	List<Order> findByMemberMemberIdAndOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(String memberId,
			OrderStatus status, LocalDateTime startDateTime, LocalDateTime endDateTime);

	Order findByMemberMemberIdAndId(String memberId, Long id);

	int countByMemberMemberIdAndOrderInfosOrderStatusAndPaidAtBetween(String memberId, OrderStatus status,
			LocalDateTime startDateTime, LocalDateTime endDateTime);


	int countByMemberMemberIdAndPaidAtBetween(String memberId, LocalDateTime startDateTime, LocalDateTime endDateTime);

	@Query("SELECT SUM(o.paidAmount) FROM Order o WHERE o.member.memberId = :memberId AND o.paidAt BETWEEN :startDateTime AND :endDateTime")
	BigDecimal calculateTotalPaidAmountByMemberMemberIdAndPaidAtBetween(String memberId, LocalDateTime startDateTime,
			LocalDateTime endDateTime);

	List<Order> findByStatusAndPaidAtBefore(String string, LocalDateTime minusHours);

	List<Order> findByMemberMemberIdAndStatusAndPaidAtBetween(String memberId, String string,
			LocalDateTime startDateTime, LocalDateTime endDateTime);

	List<Order> findByPaidAtBetweenOrderByPaidAtDesc(LocalDateTime startDateTime, LocalDateTime endDateTime);

	List<Order> findByOrderInfosOrderStatusAndPaidAtBetweenOrderByPaidAtDesc(OrderStatus status,
			LocalDateTime startDateTime, LocalDateTime endDateTime);

	List<Order> countByOrderInfosOrderStatusAndPaidAtBetween(OrderStatus status, LocalDateTime startDateTime,
			LocalDateTime endDateTime);

	Optional<Order> findByIdAndMemberMemberId(Long id, String memberId);

	Optional<Order> findByMerchantUid(String merchantUid);

}
