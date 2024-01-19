package noninoni.Specification;

import org.springframework.data.jpa.domain.Specification;
import noninoni.entity.Product;

public class ProductSpecification {

    public static Specification<Product> hasCategory(String category) {
        return (product, cq, cb) -> {
            if (category == null || category.isEmpty() || category.equals("ALL")) {
                return cb.isTrue(cb.literal(true)); // 모든 카테고리에 대응
            }
            return cb.equal(product.get("category"), category);
        };
    }

    public static Specification<Product> hasKeyword(String keyword) {
        return (product, cq, cb) -> {
            if (keyword == null || keyword.isEmpty()) {
                return cb.isTrue(cb.literal(true)); // 모든 키워드에 대응
            }
            return cb.like(product.get("name"), "%" + keyword + "%");
        };
    }

    public static Specification<Product> hasStatus(String status) {
        return (product, cq, cb) -> {
            if (status == null || status.isEmpty()) {
                return cb.isTrue(cb.literal(true)); // 모든 상태에 대응
            }
            return cb.equal(product.get("status"), Product.ProductStatus.valueOf(status));
        };
    }
}
