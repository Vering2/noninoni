package noninoni.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant; // 상품 변형 추가

    private int quantity;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    // 상품, 상품 변형, 수량 및 장바구니를 받는 생성자
    public CartItem(Product product, ProductVariant variant, int quantity, Cart cart) {
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
        setCart(cart);
    }

    // 장바구니 설정 및 장바구니 아이템 목록에 추가
    public void setCart(Cart cart) {
        this.cart = cart;
        if (cart != null && !cart.getItems().contains(this)) {
            cart.getItems().add(this);
        }
    }

    // 기본 생성자
    public CartItem() {
        // 기본 생성자 내용
    }

	// 새로운 생성자 추가
    public CartItem(Product product, ProductVariant variant, int quantity) {
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
    }

    // 생성자, getters, setters
}
