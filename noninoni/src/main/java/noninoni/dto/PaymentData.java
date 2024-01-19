package noninoni.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class PaymentData {
    private List<OrderInfoDTO> orderInfo; // 구매한 상품 정보 리스트
    private String memberId;
    private String recipient;
    private String recphoneNumber;
    private String recaddress;
    private String omessage;
    private Long orderId;
    private String productNames;
    private BigDecimal shippingCost;
    private String merchantUid;

    // Getter, Setter
}