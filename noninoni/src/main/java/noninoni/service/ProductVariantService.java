package noninoni.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import noninoni.dto.OrderInfoDTO;
import noninoni.entity.Product;
import noninoni.entity.ProductVariant;
import noninoni.repository.ProductVariantRepository;

@Slf4j
@Service
@Transactional
public class ProductVariantService {

	private final ProductVariantRepository productVariantRepository;

	@Autowired
	private ProductService productService;

	@Autowired
	public ProductVariantService(ProductVariantRepository productVariantRepository) {
		this.productVariantRepository = productVariantRepository;
	}

	public ProductVariant getProductVariant(Long variantId) {
		return productVariantRepository.findById(variantId).orElse(null);
	}

	public ProductVariant getProductVariant(Product product, Long colorId, Long sizeId) {
		return productVariantRepository.findByProductAndColorIdAndSizeId(product, colorId, sizeId)
				.orElseThrow(() -> new EntityNotFoundException("ProductVariant not found for product: " + product
						+ ", colorId: " + colorId + ", sizeId: " + sizeId));
	}

	public ProductVariant getProductVariant(Long productId, Long colorId, Long sizeId) {
		// TODO Auto-generated method stub
		Product product = productService.getProduct(productId);
		return productVariantRepository.findByProductAndColorIdAndSizeId(product, colorId, sizeId)
				.orElseThrow(() -> new EntityNotFoundException("ProductVariant not found for product: " + product
						+ ", colorId: " + colorId + ", sizeId: " + sizeId));
	}

	public boolean checkAndDecreaseStock(List<OrderInfoDTO> orderInfoList) {
		// 재고 확인
		for (OrderInfoDTO orderInfo : orderInfoList) {
			Optional<ProductVariant> variantOpt = productVariantRepository.findById(orderInfo.getVariantId());
			if (variantOpt.isEmpty() || variantOpt.get().getStockQuantity() < orderInfo.getQuantity()) {
				return false; // 재고 부족
			}
		}

		// 재고 감소
		for (OrderInfoDTO orderInfo : orderInfoList) {
			ProductVariant variant = productVariantRepository.findById(orderInfo.getVariantId())
					.orElseThrow(() -> new IllegalStateException("제품 변형을 찾을 수 없음"));
			int newStock = variant.getStockQuantity() - orderInfo.getQuantity();
			if (newStock < 0) {
				throw new IllegalStateException("재고 부족");
			}
			variant.setStockQuantity(newStock);
			productVariantRepository.save(variant);
		}
		return true; // 재고 충분
	}

	public void restoreStock(List<OrderInfoDTO> orderInfoDTOs) {
		// TODO Auto-generated method stub
		for (OrderInfoDTO orderInfo : orderInfoDTOs) {
			ProductVariant variant = productVariantRepository.findById(orderInfo.getVariantId())
					.orElseThrow(() -> new IllegalStateException("Invalid variant ID"));
			variant.setStockQuantity(variant.getStockQuantity() + orderInfo.getQuantity());
			productVariantRepository.save(variant);
		}
	}

	public Map<String, Integer> getAvailableQuantities(List<OrderInfoDTO> orderInfoList) {
		Map<String, Integer> availableQuantities = new HashMap<>();

		String productKey = null;
		for (OrderInfoDTO orderInfo : orderInfoList) {
			Optional<ProductVariant> variantOpt = productVariantRepository.findById(orderInfo.getVariantId());
			if (variantOpt.isPresent()) {
				ProductVariant variant = variantOpt.get();
				int availableQuantity = Math.max(0, variant.getStockQuantity());
				productKey = variant.getProduct().getName() + " " + variant.getColor().getName() + " "
						+ variant.getSize().getName();
				availableQuantities.put(productKey, availableQuantity);
			} 
		}

		return availableQuantities;
	}

	// 기타 서비스 메서드...
}
