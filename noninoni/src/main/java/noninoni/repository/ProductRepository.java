package noninoni.repository;

import noninoni.entity.Product;
import noninoni.entity.Product.ProductStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

	// 카테고리에 따른 상품 조회
	Page<Product> findByCategory(String category, PageRequest pageRequest);

	List<Product> findByCategory(String category);

	Page<Product> findByNameContaining(String keyword, PageRequest pageRequest);

	Page<Product> findByCategoryAndNameContaining(String category, String name, PageRequest pageRequest);

	@Query("SELECT p FROM Product p WHERE p.mainimageUrl LIKE %:imageUrl%")
	Product findByMainImageUrlContaining(@Param("imageUrl") String imageUrl);

	long countByCategoryContainingAndNameContaining(String category, String keyword);

	Optional<Product> findById(Long Id);

	long countByCategory(String category);

	long countByNameContaining(String keyword);

	long countByCategoryAndNameContaining(String category, String keyword);

	long count();

	@Query("SELECT p FROM Product p WHERE p.id IN :ids")
	Page<Product> findAllByIds(@Param("ids") List<Long> ids, Pageable pageable);

	Page<Product> findByIdIn(List<Long> productIds, Pageable pageable);

	Page<Product> findByStatusAndCategoryAndNameContaining(ProductStatus availableStatus, String category,
			String keyword, PageRequest pageRequest);

	Page<Product> findByStatusAndNameContaining(ProductStatus availableStatus, String keyword, PageRequest pageRequest);

	Page<Product> findByStatusAndCategory(ProductStatus availableStatus, String category, PageRequest pageRequest);

	Page<Product> findByStatus(ProductStatus availableStatus, PageRequest pageRequest);

	Page<Product> findByStatusAndMainCategory(ProductStatus availableStatus, String categoryType,
			PageRequest pageRequest);

	List<Product> findByMainCategory(String oldType);

	@Query("SELECT p FROM Product p JOIN p.variants v WHERE v.stockQuantity <= :stock")
	Page<Product> findByStockQuantityLessThanEqual(@Param("stock") Integer stock, Pageable pageable);

}
