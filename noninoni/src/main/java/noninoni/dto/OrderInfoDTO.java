package noninoni.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderInfoDTO {
	private Long productId; // 상품 ID
	private String productName; // 상품명
	private Integer quantity; // 수량
	private BigDecimal price; // 가격
	private BigDecimal sumPrice; // 가격
	private Long variantId;
	private Long orderId;
	

	// 수량과 가격을 곱하여 합계 가격을 계산하는 메소드
	public BigDecimal calculateSumPrice() {
		if (price != null && quantity != null) {
			sumPrice = price.multiply(BigDecimal.valueOf(quantity));
		}
		return sumPrice;
	}

	public OrderInfoDTO(Long productId, Long variantId, Integer quantity) {
		this.productId = productId;
		this.variantId = variantId;
		this.quantity = quantity;
		// TODO Auto-generated constructor stub
	}
}
