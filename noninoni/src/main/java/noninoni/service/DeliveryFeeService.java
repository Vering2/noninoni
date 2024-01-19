package noninoni.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import noninoni.entity.DeliveryFee;
import noninoni.repository.DeliveryFeeRepository;

@Service
public class DeliveryFeeService {
	@Autowired
	DeliveryFeeRepository deliveryFeeRepository;

	public void updateDeliveryFee(Long id, BigDecimal fee, BigDecimal minimum) {
        DeliveryFee deliveryFee = deliveryFeeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Delivery fee not found"));

        deliveryFee.setFee(fee);
        deliveryFee.setMinimum(minimum);
        deliveryFeeRepository.save(deliveryFee);
    }

	public DeliveryFee findbyId(long id) {
	    return deliveryFeeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("DeliveryFee not found"));
	}


}
