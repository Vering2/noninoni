package noninoni.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import noninoni.entity.Favorite;
import noninoni.entity.Member;
import noninoni.entity.Product;
import noninoni.repository.FavoriteRepository;
import noninoni.repository.MemberRepository;
import noninoni.repository.ProductRepository;

@Service
@Transactional
public class FavoriteService {
	@Autowired
	private FavoriteRepository favoriteRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ProductRepository productRepository;

	public void addFavorite(String memberId, Long productId) {
		// memberId와 productId에 해당하는 Member와 Product 엔티티를 찾음
		Member member = memberRepository.findById(memberId).orElse(null);
		Product product = productRepository.findById(productId).orElse(null);

		if (member != null && product != null) {
			Favorite favorite = new Favorite();
			favorite.setMember(member);
			favorite.setProduct(product);

			favoriteRepository.save(favorite);
		}
	}

	public List<Long> getFavoriteProductIdsByMemberId(String memberId) {
		List<Favorite> favorites = favoriteRepository.findByMemberMemberId(memberId);
		return favorites.stream().map(favorite -> favorite.getProduct().getId()).collect(Collectors.toList());
	}

	public void removeFavorite(String memberId, Long productId) {
		favoriteRepository.deleteByMemberMemberIdAndProductId(memberId, productId);
	}

	public Page<Product> getFavoriteProductsByMemberId(String memberId, Pageable pageable) {
		// 즐겨찾기 페이징 조회 없이 전체 조회
		List<Favorite> favorites = favoriteRepository.findByMemberMemberId(memberId);

		// 즐겨찾기된 상품 ID 목록 추출
		List<Long> productIds = favorites.stream().map(favorite -> favorite.getProduct().getId())
				.collect(Collectors.toList());

		if (productIds.isEmpty()) {
			// 즐겨찾기된 상품이 없는 경우 빈 페이지 반환
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}

		// 상품 ID 목록에 해당하는 상품 페이징 조회
		Page<Product> productsPage = productRepository.findByIdIn(productIds, pageable);

		return productsPage;
	}

	public List<Long> getFavoriteProductIdsForProducts(String memberId, List<Long> productIds) {
		return favoriteRepository.findFavoriteProductIdsByMemberIdAndProductIds(memberId, productIds);
	}

	 public boolean isProductFavoritedByMember(String memberId, Long productId) {
        // 이 메소드는 memberId가 productId 상품을 즐겨찾기했는지 확인합니다.
        return favoriteRepository.existsByMemberMemberIdAndProductId(memberId, productId);
    }

}
