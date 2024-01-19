package noninoni.dto;


import lombok.Data;
import noninoni.entity.CartItem;
import noninoni.entity.Product;
import noninoni.entity.ProductVariant;

@Data
public class CartItemDto {
	
	
    private Long id;
    private int quantity;
    private Product product;
    private ProductVariant variant; // 상품 변형 정보 추가

    // 상품과 상품 변형 정보, 수량을 받는 생성자
    public CartItemDto(Product product, ProductVariant variant, int quantity) {
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
    }

    // CartItem 엔티티로부터 CartItemDto 생성
    public CartItemDto(CartItem cartItem) {
        this.id = cartItem.getId();
        this.quantity = cartItem.getQuantity();
        this.product = cartItem.getProduct(); // Product 엔티티 참조
        this.variant = cartItem.getVariant(); // ProductVariant 엔티티 참조
    }

    // 기본 생성자
    public CartItemDto() {
        // 기본 생성자 내용
    }
    // getter 메서드...
}
