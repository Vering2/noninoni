package noninoni.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private BigDecimal price;

	@Column(length = 1000)
	private String description;

	@Column(name = "category")
	private String category;

	@Column(name = "main_category")
	private String mainCategory;

	@Column(name = "mainimage_url")
	private String mainimageUrl;

	@Column(name = "subimage_url")
	private String subimageUrl;

	@Enumerated(EnumType.STRING)
	private ProductStatus status;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<ProductVariant> variants;
	// Getters and setters

	public enum ProductStatus {
		AVAILABLE, OUT_OF_STOCK, DISCONTINUED
	}

	// Product 클래스에 이미지 URL 추가 메소드
	public void addMainImageUrl(String mainImageUrlori, String joinedMainImageFileNames) {
		if (mainImageUrlori == null || mainImageUrlori.isEmpty()) {
			this.mainimageUrl = joinedMainImageFileNames;
		} else {
			this.mainimageUrl = mainImageUrlori + "," + joinedMainImageFileNames;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Product product = (Product) o;
		return Objects.equals(id, product.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	// Standard getters and setters
}
