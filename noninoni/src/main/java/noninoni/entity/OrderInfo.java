package noninoni.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class OrderInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	private Long productId; // 상품 ID
	private String productName; // 상품명
	private Integer quantity; // 수량
	private BigDecimal price; // 단가
	private BigDecimal sumPrice; // 총액
	private String trackNumber;
	
	@ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant; // 상품 변형 추가

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus; // 주문 상태

	private boolean orderConfirm; // 구매 확정 여부
	private Long reviewId; // 연결된 리뷰의 ID

	// 여기에 필요한 getter, setter 메소드 추가
}