package noninoni.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import noninoni.dto.CartItemDto;
import noninoni.entity.Cart;
import noninoni.entity.CartItem;
import noninoni.entity.Member;
import noninoni.entity.OrderInfo;
import noninoni.entity.Product;
import noninoni.entity.ProductVariant;
import noninoni.repository.CartItemRepository;
import noninoni.repository.CartRepository;
import noninoni.repository.MemberRepository;
import noninoni.repository.ProductVariantRepository;

@Slf4j
@Service
public class CartService {
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductVariantService productVariantService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	// 필요한 레포지토리와 서비스 주입
	private final ProductVariantRepository productVariantRepository;

	// 생성자 또는 @Autowired를 통한 의존성 주입
	public CartService(ProductVariantRepository productVariantRepository) {
		this.productVariantRepository = productVariantRepository;
	}

	public Cart getCart(Long cartId) {
		return cartRepository.findById(cartId).orElse(null);
	}

	public Cart getOrCreateCart(Member member) {
		Cart cart = cartRepository.findByMember(member);

		if (cart == null) {
			cart = new Cart();
			cart.setMember(member);
			cartRepository.save(cart);
		}

		return cart;
	}

	@Transactional
	public void deleteCartItem(Long cartItemId) {
		CartItem cartItem = cartItemRepository.findById(cartItemId)
				.orElseThrow(() -> new EntityNotFoundException("CartItem not found: " + cartItemId));

		Cart cart = cartItem.getCart();
		cart.getItems().remove(cartItem); // Cart 엔티티에서 CartItem 제거

		cartItemRepository.delete(cartItem); // CartItem 엔티티 삭제
	}

	public void updateCartItemQuantity(Long itemId, int quantity) {
		Optional<CartItem> cartItemOptional = cartItemRepository.findById(itemId);
		if (cartItemOptional.isPresent()) {
			CartItem cartItem = cartItemOptional.get();
			cartItem.setQuantity(quantity);
			cartItemRepository.save(cartItem);
		} else {
			// 항목이 존재하지 않는 경우의 처리
		}
	}

	public void mergeCart(Member member, List<CartItemDto> cartItems) {
		Cart memberCart = member.getCart();
		if (memberCart == null) {
			memberCart = new Cart();
			member.setCart(memberCart);
		}

		for (CartItemDto tempItemDto : cartItems) {
			Product product = productService.getProduct(tempItemDto.getProduct().getId());
			ProductVariant variant = productVariantService.getProductVariant(tempItemDto.getVariant().getId());
			CartItem existingItem = findItemInCart(memberCart, product, variant);

			if (existingItem != null) {
				// 기존에 있는 상품이면 수량을 합산
				existingItem.setQuantity(existingItem.getQuantity() + tempItemDto.getQuantity());
			} else {
				// 새로운 상품이면 장바구니에 추가
				CartItem newItem = new CartItem(product, variant, tempItemDto.getQuantity(), memberCart);
				memberCart.addItem(newItem);
			}
		}
		cartRepository.save(memberCart);
	}

	private CartItem findItemInCart(Cart cart, Product product, ProductVariant variant) {
		return cart.getItems().stream()
				.filter(item -> item.getProduct().equals(product) && item.getVariant().equals(variant)).findFirst()
				.orElse(null);
	}

	public List<CartItemDto> getCartItemsWithProductDetails(Long cartId) {
		// cartId를 사용하여 장바구니 항목 조회
		List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);

		// 각 CartItem의 Product 정보를 포함하는 DTO 리스트 생성
		return cartItems.stream().map(item -> new CartItemDto(item)).collect(Collectors.toList());
	}

	@Transactional
	public void clearCartItems(String memberId, List<OrderInfo> orderInfo) {
		// memberId를 사용하여 Member 객체를 조회
		Optional<Member> memberOpt = memberRepository.findByMemberId(memberId);

		if (!memberOpt.isPresent()) {
			// Member가 존재하지 않는 경우, 처리를 중단하거나 적절한 예외 처리를 수행
			return;
		}

		Member member = memberOpt.get();
		Cart cart = cartRepository.findByMember(member);
		List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

		List<CartItem> itemsToRemove = new ArrayList<>();
		for (OrderInfo purchasedItem : orderInfo) {
			cartItems.forEach(cartItem -> {
				ProductVariant productVariant = cartItem.getVariant();
				if (productVariant != null && productVariant.getId() != null
						&& productVariant.getId().equals(purchasedItem.getVariant().getId())) {
					itemsToRemove.add(cartItem);
				}
			});
		}

		if (!itemsToRemove.isEmpty()) {
			for (CartItem cartItem : itemsToRemove) {
				cart = cartItem.getCart();
				cart.getItems().remove(cartItem); // Cart 엔티티에서 CartItem 제거
				cartItemRepository.delete(cartItem); // CartItem 엔티티 삭제
			}
			cartItems.removeAll(itemsToRemove); // 메모리 상의 리스트에서도 항목 제거
		}

	}

	public void addToCart(Long cartId, ProductVariant variant, int quantity) {
		// 장바구니를 찾거나 생성
		Cart cart = cartRepository.findById(cartId).orElseGet(() -> new Cart());

		// 장바구니에 이미 해당 상품 변형(variant)이 있는지 확인
		CartItem existingItem = cart.getItems().stream()
				.filter(item -> item.getProduct().equals(variant.getProduct()) && item.getVariant().equals(variant))
				.findFirst().orElse(null);

		if (existingItem != null) {
			// 이미 있는 상품이면 수량을 더함
			existingItem.setQuantity(existingItem.getQuantity() + quantity);
		} else {
			// 새 상품이면 장바구니에 추가
			CartItem newItem = new CartItem(variant.getProduct(), variant, quantity);
			cart.addItem(newItem);
		}

		// 장바구니 저장
		cartRepository.save(cart);
	}

	@Transactional
	public List<CartItemDto> getCartItemsWithProductVariantDetails(Long cartId, List<Long> productIds,
			List<Long> colorIds, List<Long> sizeIds) {
		// cartId의 유효성 검증
		cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("Invalid cart ID"));

		// 모든 조합에 대한 ProductVariant 목록을 조회하는 쿼리
		List<ProductVariant> productVariantList = productVariantRepository
				.findByProductIdInAndColorIdInAndSizeIdIn(productIds, colorIds, sizeIds);

		List<Long> productVariantIds = productVariantList.stream().map(ProductVariant::getId)
				.collect(Collectors.toList());

		List<CartItem> cartItems = cartItemRepository.findByCartIdAndVariantIdIn(cartId, productVariantIds);

		return cartItems.stream().map(CartItemDto::new).collect(Collectors.toList());
	}

	@Transactional
	public void addOrUpdateProduct(Long cartId, ProductVariant variant, Integer quantity) {
		// 장바구니 내 상품 찾기
		Cart cart = cartRepository.findById(cartId).orElseGet(() -> new Cart());

		// 장바구니에 이미 해당 상품 변형(variant)이 있는지 확인
		CartItem existingItem = cart.getItems().stream()
				.filter(item -> item.getProduct().equals(variant.getProduct()) && item.getVariant().equals(variant))
				.findFirst().orElse(null);

		if (existingItem != null) {
			// 이미 있는 상품이면 수량을 더함
			existingItem.setQuantity(quantity);
		} else {
			// 새 상품이면 장바구니에 추가
			CartItem newItem = new CartItem(variant.getProduct(), variant, quantity);
			cart.addItem(newItem);
		}
		// 장바구니 업데이트 로직 (예: 데이터베이스 저장)
		cartRepository.save(cart);
	}

	@Transactional
	public int getCartItemCount(Long cartId) {
		// TODO Auto-generated method stub
		return cartItemRepository.countByCartId(cartId);
	}


}