package noninoni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import noninoni.entity.DeliveryAddress;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {

	List<DeliveryAddress> findByMemberId(String memberId);
}