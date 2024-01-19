package noninoni.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import noninoni.entity.OrderInfo;
import noninoni.repository.OrderInfoRepository;

@Service
public class OrderInfoService {

    @Autowired
    private OrderInfoRepository orderInfoRepository;

    @Transactional(readOnly = true)
    public OrderInfo findById(Long id) {
        return orderInfoRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(OrderInfo orderInfo) {
        orderInfoRepository.save(orderInfo);
    }

	@Transactional(readOnly = true)
    public OrderInfo findByReviewId(Long reviewId) {
        return orderInfoRepository.findByReviewId(reviewId);
    }

	public List<OrderInfo> findByProductNameContaining(String searchTerm) {
		// TODO Auto-generated method stub
		return orderInfoRepository.findByProductNameContaining(searchTerm);
	}

	public List<OrderInfo> findByProductId(Long id) {
		// TODO Auto-generated method stub
		return orderInfoRepository.findByProductId(id);
	}


}