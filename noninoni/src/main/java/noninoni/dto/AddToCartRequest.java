package noninoni.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Long productId;
    private Long colorId;
    private Long sizeId;
    private Integer quantity;

    // 생성자, getter 및 setter 생략
}
