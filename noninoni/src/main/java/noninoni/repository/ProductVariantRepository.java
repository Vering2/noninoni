package noninoni.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import noninoni.entity.Color;
import noninoni.entity.Product;
import noninoni.entity.ProductVariant;
import noninoni.entity.Size;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

	Optional<ProductVariant> findByProductIdAndColorIdAndSizeId(Long id, Long colorId, Long sizeId);
	
	
	// 필요한 경우 추가 메소드를 정의할 수 있습니다.

	List<ProductVariant> findByProductId(Long id);

	@Query("SELECT pv.size FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.color.id = :colorId")
	List<Size> findSizesByProductIdAndColorId(@Param("productId") Long productId, @Param("colorId") Long colorId);

	@Query("SELECT DISTINCT pv.color FROM ProductVariant pv WHERE pv.product.id = ?1")
	List<Color> findDistinctColorsByProductId(Long productId);


	Optional<ProductVariant> findByProductAndColorIdAndSizeId(Product product, Long colorId, Long sizeId);


	List<ProductVariant> findByProductIdInAndColorIdInAndSizeIdIn(List<Long> productIds, List<Long> colorIds,
			List<Long> sizeIds);


	List<ProductVariant> findByProductIdAndStockQuantityGreaterThan(Long productId, int i);


	List<ProductVariant> findByProductIdAndColorIdAndStockQuantityGreaterThan(Long productId, Long colorId, int i);


	void deleteByProductId(Long productId);

}