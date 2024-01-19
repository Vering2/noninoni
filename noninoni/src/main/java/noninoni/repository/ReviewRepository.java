package noninoni.repository;

import noninoni.entity.OrderInfo;
import noninoni.entity.Review;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	boolean existsByOrderInfoId(Long orderInfoId);
    // 필요한 추가적인 JPA 쿼리 메서드를 여기에 정의할 수 있습니다.

	Page<Review> findByOrderInfoIn(List<OrderInfo> orderInfos, Pageable pageable);
}
