package noninoni.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import noninoni.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

	// memberId와 productId를 사용해 즐겨찾기 항목 삭제

	List<Favorite> findByMemberMemberId(String memberId);

	void deleteByMemberMemberIdAndProductId(String memberId, Long productId);

	Page<Favorite> findByMemberMemberId(String memberId, Pageable pageable);

	void deleteByProductId(Long productId);

	// 새로운 메서드: 특정 회원이 특정 상품들을 즐겨찾기했는지 조회
	@Query("SELECT f.product.id FROM Favorite f WHERE f.member.memberId = :memberId AND f.product.id IN :productIds")
	List<Long> findFavoriteProductIdsByMemberIdAndProductIds(@Param("memberId") String memberId,
			@Param("productIds") List<Long> productIds);

	boolean existsByMemberMemberIdAndProductId(String memberId, Long productId);

}