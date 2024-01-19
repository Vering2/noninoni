package noninoni.dto;

import java.time.LocalDateTime;


import lombok.Data;
import noninoni.entity.Review;

@Data
public class ReviewDto {
    private Long id;
    private String title;
    private String text;
    private int rating;
    private String imageUrls;
    private LocalDateTime createdDate;
    private String memberName;
    private String color;
    private String size;

    // Review 객체를 받아 필요한 정보로 변환하는 생성자
    public ReviewDto(Review review) {
        this.id = review.getId();
        this.title = review.getTitleName();
        this.text = review.getText();
        this.rating = review.getRating();
        this.imageUrls = review.getImageUrl();
        this.createdDate = review.getCreatedDate();
        this.memberName = review.getOrderInfo().getOrder().getMember().getName();
        this.color = review.getOrderInfo().getVariant().getColor().getName();
        this.size = review.getOrderInfo().getVariant().getSize().getName();
    }

    // Getters and Setters
}