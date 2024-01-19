package noninoni.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;

@Entity
@Table(name = "reviews")
@Data
public class Review {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String titleName;
	private String text;
	private String imageUrl;

	@Column(nullable = false)
	private boolean hidden = false; // 숨김 여부

	@ManyToOne
	@JoinColumn(name = "order_info_id", nullable = false)
	private OrderInfo orderInfo;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdDate; // 리뷰 작성 날짜

	@Column(nullable = false)
	private Integer rating; // 별점

	// Getter, Setter...
}
