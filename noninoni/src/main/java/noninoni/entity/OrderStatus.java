package noninoni.entity;

public enum OrderStatus {
	PAYMENT_PENDING("입금 전"), PREPARING_SHIPMENT("배송 준비중"), SHIPPED("배송중"), DELIVERED("배송완료"), CANCELED("취소"),
	EXCHANGED("교환"), RETURNED("반품"), TEMPORARY("임시"), EXCHANGED_DELIVERED("교환완료"); // 추가된 임시 상태

	private final String description;

	OrderStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
