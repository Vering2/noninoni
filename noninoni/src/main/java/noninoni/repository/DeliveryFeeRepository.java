package noninoni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import noninoni.entity.DeliveryFee;

public interface DeliveryFeeRepository extends JpaRepository<DeliveryFee, Long> {
}