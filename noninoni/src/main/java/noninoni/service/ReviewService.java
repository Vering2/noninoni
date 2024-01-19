package noninoni.service;

import noninoni.entity.OrderInfo;
import noninoni.entity.Review;
import noninoni.repository.ReviewRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

	private final ReviewRepository reviewRepository;

	@Autowired
	public ReviewService(ReviewRepository reviewRepository) {
		this.reviewRepository = reviewRepository;
	}

	@Transactional
	public Review save(Review review) {
		return reviewRepository.save(review);
	}

	@Transactional(readOnly = true)
	public Review findById(Long id) {
		return reviewRepository.findById(id).orElse(null);
	}

	public Page<Review> findAll(Pageable pageable) {
		return reviewRepository.findAll(pageable);
	}

	public boolean hasReview(Long orderInfoId) {
		// 주문 정보에 대한 리뷰가 있는지 확인하는 로직
		// 예: 리뷰 리포지토리에서 주문 정보 ID를 사용하여 리뷰 검색
		return reviewRepository.existsByOrderInfoId(orderInfoId);
	}

	public Page<Review> findByOrderInfo(List<OrderInfo> orderInfos, Pageable pageable) {
		return reviewRepository.findByOrderInfoIn(orderInfos, pageable);
	}

	public void toggleReviewHiddenStatus(Long reviewId) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid review ID"));
		review.setHidden(!review.isHidden());
		reviewRepository.save(review);
	}

	public Review getReviewById(Long reviewId) {
		// 데이터베이스에서 리뷰 ID로 리뷰 검색
		return reviewRepository.findById(reviewId)
				.orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
	}

	 @Transactional
    public void updateReview(Review review) {
        // 데이터베이스에서 기존 리뷰를 검색
        Review existingReview = reviewRepository.findById(review.getId()).orElseThrow(() -> new RuntimeException("Review not found"));

        // 기존 리뷰 정보 업데이트
        existingReview.setTitleName(review.getTitleName());
        existingReview.setText(review.getText());
        existingReview.setRating(review.getRating());

        // 새 이미지 정보가 있다면 처리 (예: 이미지 경로 설정)
        // if (newImagePath != null) {
        //     existingReview.setImageUrl(newImagePath);
        // }

        // 리뷰 정보 저장
        reviewRepository.save(existingReview);
    }
}
