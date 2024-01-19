package noninoni.dto;

public class SizeDto {
    private Long id;
    private String name;
    private int additionalPrice;

    // 기본 생성자
    public SizeDto() {
    }

    // 모든 필드를 초기화하는 생성자
    public SizeDto(Long id, String name, int additionalPrice) {
        this.id = id;
        this.name = name;
        this.additionalPrice = additionalPrice;
    }

    // Getter 및 Setter 메소드
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAdditionalPrice() {
        return additionalPrice;
    }

    public void setAdditionalPrice(int additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    // toString(), hashCode(), equals() 메소드는 필요에 따라 구현합니다.
}
