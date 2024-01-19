package noninoni.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import noninoni.dto.CartItemDeleteRequest;
import noninoni.dto.CustomUserDetails;
import noninoni.entity.Cart;
import noninoni.entity.Product;
import noninoni.entity.ProductVariant;
import noninoni.service.CartService;
import noninoni.service.ProductService;
import noninoni.service.ProductVariantService;

@Slf4j
@RestController
@RequestMapping("/cart")
public class CartController extends BaseController {
	@Autowired
	private CartService cartService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductVariantService productVariantService;

	@GetMapping("/api/cart/count")
	@ResponseBody
	public int getCartItemCount(HttpServletRequest request) {
		HttpSession session = request.getSession();
		CustomUserDetails user = getCurrentUser();
		int itemCount = 0;

		if (user != null) {
			// 로그인한 사용자: 데이터베이스에서 장바구니 아이템 수 계산
			Long cartId = (Long) session.getAttribute("cartId");
			if (cartId != null) {
				itemCount = cartService.getCartItemCount(cartId);
			}
		} else {
			// 비로그인 사용자 처리
			Map<String, Integer> sessionCart = (Map<String, Integer>) session.getAttribute("cart");
			if (sessionCart != null) {
				itemCount = sessionCart.size(); // 각 상품 조합을 하나의 아이템으로 카운트
			}

		}

		// 계산된 장바구니 아이템 수를 세션에 저장
		session.setAttribute("cartItemCount", itemCount);

		return itemCount;
	}

	@PostMapping("/add")
	public ResponseEntity<?> addToCart(HttpSession session, @RequestParam(required = false) Long cartId,
			@RequestParam("productId") Long productId, @RequestParam("colorId") Long colorId,
			@RequestParam("sizeId") Long sizeId, @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
		// 현재 로그인한 사용자 정보를 가져옴
		CustomUserDetails user = getCurrentUser();

		// 로그인 상태 확인
		if (user != null) {
			// 로그인한 경우: 데이터베이스에 저장
			Product product = productService.getProduct(productId);
			ProductVariant variant = productVariantService.getProductVariant(product, colorId, sizeId);
			cartService.addToCart(cartId, variant, quantity);
		} else {
			// 비로그인 상태: 세션에 저장
			Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
			if (cart == null) {
				cart = new HashMap<>();
			}
			// 상품 ID, 색상 ID, 사이즈 ID를 키로 사용
			String cartKey = productId + ":" + colorId + ":" + sizeId;

			// 기존 수량에 추가
			int existingQuantity = cart.getOrDefault(cartKey, 0);
			cart.put(cartKey, existingQuantity + quantity);
			session.setAttribute("cart", cart);
		}

		return ResponseEntity.ok().build();
	}

	@GetMapping("/{cartId}")
	public ResponseEntity<Cart> getCart(@PathVariable Long cartId) {
		Cart cart = cartService.getCart(cartId);
		return ResponseEntity.ok(cart);
	}

	@PostMapping("/updateQuantities")
	public ResponseEntity<?> updateQuantities(HttpSession session,
			@RequestParam(value = "itemId", required = false) Long itemId, @RequestParam("quantity") int quantity,
			@RequestParam("productId") Long productId, @RequestParam("colorId") Long colorId,
			@RequestParam("sizeId") Long sizeId) {

		
		CustomUserDetails user = getCurrentUser();

		if (user != null) {
			// 로그인한 사용자: 데이터베이스의 카트 아이템 수량 업데이트
			cartService.updateCartItemQuantity(itemId, quantity);
		} else {
			// 비로그인 사용자: 세션의 카트 맵 업데이트
			Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
			if (cart == null) {
				cart = new HashMap<>();
			}
			String cartKey = productId + ":" + colorId + ":" + sizeId;
			cart.put(cartKey, quantity);
			session.setAttribute("cart", cart);
		}

		return ResponseEntity.ok().build();
	}

	@PostMapping("/deleteItem")
	public ResponseEntity<?> deleteItem(HttpSession session,
			@RequestParam(value = "itemId", required = false) Long itemId, @RequestParam("productId") Long productId,
			@RequestParam("colorId") Long colorId, @RequestParam("sizeId") Long sizeId) {
		CustomUserDetails user = getCurrentUser();

		if (user != null) {
			// 로그인한 사용자의 경우, 서비스를 통해 데이터베이스에서 카트 아이템 삭제
			cartService.deleteCartItem(itemId);
		} else {
			// 비로그인 사용자의 경우, 세션의 카트 맵에서 특정 항목 삭제
			Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
			if (cart != null) {
				String cartKey = productId + ":" + colorId + ":" + sizeId;
				cart.remove(cartKey);
				session.setAttribute("cart", cart);
			}
		}
		return ResponseEntity.ok().build();
	}

	@PostMapping("/deleteSelected")
	public ResponseEntity<?> deleteSelected(HttpSession session, @RequestBody CartItemDeleteRequest deleteRequest) {
		CustomUserDetails user = getCurrentUser();

		if (user != null) {
			// For logged-in users, delete items using the service
			for (Long itemId : deleteRequest.getItemIds()) {
				cartService.deleteCartItem(itemId);
			}
		} else {
			// For non-logged-in users, delete items from the session cart
			Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
			if (cart != null) {
				for (String cartKey : deleteRequest.getCartKeys()) {
					cart.remove(cartKey);
				}
				session.setAttribute("cart", cart);
			}
		}
		return ResponseEntity.ok().build();
	}

	@PostMapping("/add-to-cart-and-checkout")
	public ResponseEntity<?> addToCartAndCheckout(HttpSession session, @RequestParam(required = false) Long cartId,
			@RequestParam("productId") Long productId, @RequestParam("colorId") Long colorId,
			@RequestParam("sizeId") Long sizeId, @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
		CustomUserDetails user = getCurrentUser();

		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		// 로그인 상태 확인
		Product product = productService.getProduct(productId);
		ProductVariant variant = productVariantService.getProductVariant(product, colorId, sizeId);

		cartService.addOrUpdateProduct(cartId, variant, quantity);
		return ResponseEntity.ok().build();
	}

}
