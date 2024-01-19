package noninoni.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import noninoni.entity.DeliveryAddress;
import noninoni.entity.Member;
import noninoni.repository.DeliveryAddressRepository;
import noninoni.repository.MemberRepository;

@Slf4j
@Service
public class DeliveryAddressService {
	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private DeliveryAddressRepository deliveryAddressRepository;

	// 사용자의 배송지 목록 조회
	public List<DeliveryAddress> getMemberDeliveryAddresses(String memberId) {
		return deliveryAddressRepository.findByMemberId(memberId);
	}

	// 새로운 배송지 저장
	public void saveDeliveryAddress(DeliveryAddress deliveryAddress, String memberId) {
		// 여기에서 memberId를 사용하여 배송지 정보에 사용자 식별자를 설정할 수 있습니다.
		deliveryAddress.setMemberId(memberId);
		deliveryAddressRepository.save(deliveryAddress);
	}

	public DeliveryAddress getDefaultaddress(DeliveryAddress defaultAddress) {
		// findById는 Optional 객체를 반환합니다.
		Optional<DeliveryAddress> existingDeliveryAddress = deliveryAddressRepository.findById(defaultAddress.getId());

		// ifPresent를 사용하여 값이 존재하는 경우에만 처리하거나, orElse를 사용하여 기본값을 설정할 수 있습니다.
		return existingDeliveryAddress.orElse(null); // 값이 존재하지 않는 경우 null 반환
	}

	@Transactional
	public void deleteAddress(String memberId, Long addressId) {
		Member member = memberRepository.findByMemberId(memberId).orElse(null);
		if (member != null && member.getDefaultAddress() != null
				&& member.getDefaultAddress().getId().equals(addressId)) {
			member.setDefaultAddress(null);
			memberRepository.save(member);
		}
		// 해당 행을 삭제
		deliveryAddressRepository.deleteById(addressId);
	}

}