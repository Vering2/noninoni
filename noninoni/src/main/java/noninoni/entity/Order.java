package noninoni.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders") // 'order' 대신 'orders' 또는 다른 이름 사용
public class Order {
	@Id
	private Long id; // 식별자 필드 추가
	private String impUid; // 결제 고유 번호
	private String payMethod; // 결제 수단
	private String merchantUid; // 주문 번호
	private String name; // 주문자명
	private BigDecimal paidAmount; // 결제 금액
	private BigDecimal cancelAmount;
	private String applyNum; // 신용카드 승인번호
	private String status; // 결제 상태
	private String buyerEmail; // 결제시 입력한 이메일
	private String receiptUrl; // 거래 매출 전표 URL
	private String cardName; // 카드사 명
	private String bankName; // 은행 이름
	private String cardNumber; // 카드 번호
	private String recipient;
	private String recphoneNumber;
	private String recaddress;
	private String omessage;
	private String reason;
	@Column(nullable = false)
	private boolean cancellable = true; // 구매 취소 가능 여부, 기본값 true
	private BigDecimal shippingCost;
	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime paidAt; // LocalDateTime 타입으로 변경 // 결제 승인 시각 (UNIX timestamp)

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<OrderInfo> orderInfos;

	@ManyToOne
	@JoinColumn(name = "member_id")
	private Member member;

	// Getter와 Setter
}
