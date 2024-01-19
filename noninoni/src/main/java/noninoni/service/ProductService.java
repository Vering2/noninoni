package noninoni.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import noninoni.Specification.ProductSpecification;
import noninoni.dto.SizeDto;
import noninoni.entity.Color;
import noninoni.entity.Product;
import noninoni.entity.ProductVariant;
import noninoni.entity.Size;
import noninoni.repository.CartItemRepository;
import noninoni.repository.FavoriteRepository;
import noninoni.repository.ProductRepository;
import noninoni.repository.ProductVariantRepository;

@Slf4j
@Service
@Transactional
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductVariantRepository productVariantRepository;

	public Product getProduct(Long productId) {
		return productRepository.findById(productId)
				.orElseThrow(() -> new EntityNotFoundException("Product not found"));
	}

	// 모든 상품 조회
	public List<Product> findAllProducts() {
		return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
	}

	public Page<Product> getProducts(String category, String keyword, PageRequest pageRequest) {
		Product.ProductStatus availableStatus = Product.ProductStatus.AVAILABLE;

		if (keyword == null || keyword.isEmpty()) {
			if (category == null || category.isEmpty() || category.equals("ALL")) {
				return productRepository.findByStatus(availableStatus, pageRequest);
			} else {
				return productRepository.findByStatusAndCategory(availableStatus, category, pageRequest);
			}
		} else {
			if (category == null || category.isEmpty() || category.equals("ALL")) {
				return productRepository.findByStatusAndNameContaining(availableStatus, keyword, pageRequest);
			} else {
				return productRepository.findByStatusAndCategoryAndNameContaining(availableStatus, category, keyword,
						pageRequest);
			}
		}
	}

	public Page<Product> getadminProducts(String category, String keyword, PageRequest pageRequest) {

		if (keyword == null || keyword.isEmpty()) {
			if (category == null || category.isEmpty() || category.equals("ALL")) {
				return productRepository.findAll(pageRequest);
			} else {
				return productRepository.findByCategory(category, pageRequest);
			}
		} else {
			if (category == null || category.isEmpty() || category.equals("ALL")) {
				return productRepository.findByNameContaining(keyword, pageRequest);
			} else {
				return productRepository.findByCategoryAndNameContaining(category, keyword, pageRequest);
			}
		}
	}

	// 제품 정보 가져오기
	public Product getProductById(Long id) {
		return productRepository.findById(id).orElse(null);
	}

	private void updateProductImages(Product product, String imageUrl) {
		List<String> mainImageUrls = new ArrayList<>(Arrays.asList(product.getMainimageUrl().split(",")));

		mainImageUrls.remove(imageUrl);

		product.setMainimageUrl(String.join(",", mainImageUrls));
	}

	public void deleteImage(String imageUrl, Long productId) {
		// 파일 시스템 경로로 변환
		String filePath = convertProductUrlToFilePath(imageUrl);
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}

		// 제품에서 이미지 URL 제거
		Optional<Product> productOpt = productRepository.findById(productId);
		if (productOpt.isPresent()) {
			Product product = productOpt.get();

			updateProductImages(product, imageUrl); // 제품에서 이미지 URL 업데이트

			productRepository.save(product); // 변경된 제품 저장
		} else {
		}

	}

	private String convertProductUrlToFilePath(String imageUrl) {
		// 웹 서버의 기본 경로 - 이 부분은 실제 서버 환경에 맞게 수정되어야 합니다.
		String serverBasePath = "C:/Users/82106/Documents/workspace-spring-tool-suite-4-4.20.1.RELEASE/noninoni/uploaded_files/product/";

		return serverBasePath + imageUrl;
	}

	public void saveProduct(Product product) {
		// 제품 저장 로직
		productRepository.save(product);
	}

	public long countProducts(String category, String keyword) {
		boolean isCategoryEmpty = isNullOrEmpty(category);
		boolean isKeywordEmpty = isNullOrEmpty(keyword);

		if ((isCategoryEmpty || category.equals("ALL")) && isKeywordEmpty) {
			return productRepository.count();
		} else if (isCategoryEmpty || category.equals("ALL")) {
			return productRepository.countByNameContaining(keyword);
		} else if (isKeywordEmpty) {
			return productRepository.countByCategory(category);
		} else {
			return productRepository.countByCategoryAndNameContaining(category, keyword);
		}
	}

	private boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public BigDecimal getPriceById(Long productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + productId));
		return product.getPrice();
	}

	public List<Size> getSizesByProductIdAndColorId(Long productId, Long colorId) {
		return productVariantRepository.findSizesByProductIdAndColorId(productId, colorId);
	}

	public List<Color> getColorsByProductId(Long productId) {
		return productVariantRepository.findDistinctColorsByProductId(productId);
	}

	public List<Color> getAvailableColorsByProductId(Long productId) {
		// 제품 ID에 해당하는 재고가 있는 모든 제품 변형을 가져옵니다.
		List<ProductVariant> variantsWithStock = getProductVariantsWithStock(productId);

		// 해당하는 제품 변형에서 중복 없이 색상을 추출합니다.
		return variantsWithStock.stream().map(ProductVariant::getColor).distinct().collect(Collectors.toList());
	}

	public List<ProductVariant> getProductVariantsWithStock(Long productId) {
		// 재고가 0보다 큰 제품 변형을 찾습니다.
		return productVariantRepository.findByProductIdAndStockQuantityGreaterThan(productId, 0);
	}

	public Page<Product> getProductsBymainCategory(String categoryType, PageRequest pageRequest) {
		Product.ProductStatus availableStatus = Product.ProductStatus.AVAILABLE;
		return productRepository.findByStatusAndMainCategory(availableStatus, categoryType, pageRequest);
	}

	public void deleteProduct(Long productId) {
		try {
			// Then, delete the product
			Product product = getProductById(productId);

			cartItemRepository.deleteByProductId(productId);
			favoriteRepository.deleteByProductId(productId);
			productRepository.deleteById(productId);
			deleteAllImage(product.getMainimageUrl(), product.getSubimageUrl(), productId);

		} catch (Exception e) {
			// 예외 로그 출력
			log.error("상품 삭제 중 오류 발생", e);
			throw e; // 또는 적절한 예외 처리
		}
	}

	private void deleteAllImage(String mainimageUrl, String subimageUrl, Long productId) {
		// TODO Auto-generated method stub
		// mainimageUrl 분리 및 삭제
		if (mainimageUrl != null && !mainimageUrl.isEmpty()) {
			String[] mainImages = mainimageUrl.split(",");
			for (String imageUrl : mainImages) {
				deleteImage(imageUrl.trim(), productId);
			}
		}

		// subimageUrl 분리 및 삭제
		if (subimageUrl != null && !subimageUrl.isEmpty()) {
			String[] subImages = subimageUrl.split(",");
			for (String imageUrl : subImages) {
				deleteImage(imageUrl.trim(), productId);
			}
		}

	}

	@Autowired
	private FavoriteRepository favoriteRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	public void deleteCartItemandFavoriteByProductId(Long id) { // TODO Auto-generated method stub

		try {
			cartItemRepository.deleteByProductId(id);
			favoriteRepository.deleteByProductId(id);
		} catch (Exception e) {
			log.error("Error occurred while deleting cart items and favorites for product ID " + id, e);
			// 필요한 경우 예외를 다시 던지거나 다른 처리를 수행
		}

	}

	public List<SizeDto> getSizesWithStock(Long productId, Long colorId) {
		// 해당 제품 ID와 색상 ID를 가진 제품 변형 중에서 재고가 있는 것들을 찾습니다.
		List<ProductVariant> productVariants = productVariantRepository
				.findByProductIdAndColorIdAndStockQuantityGreaterThan(productId, colorId, 0);

		// 찾아낸 제품 변형들에서 Size 정보를 추출하고, SizeDto로 변환하여 리스트로 반환합니다.
		return productVariants.stream().map(variant -> new SizeDto(variant.getSize().getId(),
				variant.getSize().getName(), variant.getAdditionalPrice())).distinct().collect(Collectors.toList());
	}

	public Page<Product> getProductsByStockQuantity(Integer stock, PageRequest pageRequest) {
		return productRepository.findByStockQuantityLessThanEqual(stock, pageRequest);
	}

	public Page<Product> getProductsByFilters(String category, String keyword, String status, PageRequest pageRequest) {
		Specification<Product> spec = Specification.where(ProductSpecification.hasCategory(category))
				.and(ProductSpecification.hasKeyword(keyword)).and(ProductSpecification.hasStatus(status));

		return productRepository.findAll(spec, pageRequest);
	}

}