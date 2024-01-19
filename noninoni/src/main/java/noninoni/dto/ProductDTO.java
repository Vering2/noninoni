package noninoni.dto;

import java.math.BigDecimal;

import lombok.Data;
@Data
public class ProductDTO {

    private Long id;
    private String name;
    private BigDecimal price;
    private String mainCategory;
    private String mainimageUrl;

    // 여기에 필요한 생성자, getter, setter 추가
    // 예를 들어, 엔티티를 DTO로 변환하는 생성자나 메소드를 추가할 수 있습니다.

    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, BigDecimal price, String mainCategory, String mainimageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.mainCategory = mainCategory;
        this.mainimageUrl = mainimageUrl;
    }

    // 여기에 getter 및 setter 메소드 추가
}
