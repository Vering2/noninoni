package noninoni.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import noninoni.dto.MemberDTO;
import noninoni.entity.DeliveryAddress;
import noninoni.entity.Member;
import noninoni.repository.DeliveryAddressRepository;
import noninoni.repository.MemberRepository;

@Slf4j
@Service
public class MemberService {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private DeliveryAddressRepository deliveryAddressRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// 아이디 중복 확인 서비스
	public boolean checkDuplicateId(String memberId) {
		return memberRepository.existsByMemberId(memberId);
	}

	// 회원 가입 서비스
	public void signupMember(MemberDTO memberDTO) {
		Member member = new Member();

		member.setMemberId(memberDTO.getMemberId());
		// 비밀번호 인코딩
		// 비밀번호 인코딩하여 저장
		member.setPasswordEncoded(passwordEncoder, memberDTO.getPassword());
		member.setEmail(memberDTO.getEmail());
		member.setAddressDetails(memberDTO.getAddressDetails());
		member.setAddressPostCode(memberDTO.getAddressPostCode());
		member.setAddressRoad(memberDTO.getAddressRoad());
		member.setBankAccountNo(memberDTO.getBankAccountNo());
		member.setBankAccountOwner(memberDTO.getBankAccountOwner());
		member.setRefundBankCode(memberDTO.getRefundBankCode());
		member.setMobile(memberDTO.getMobile());
		member.setName(memberDTO.getName());
		// 여기에서 필요한 유효성 검사 등을 추가할 수 있습니다.
		memberRepository.save(member);

	}

	// 회원 아이디로 회원 조회
	public Optional<Member> findOne(String memberId) {
		return memberRepository.findByMemberId(memberId);
	}

	public boolean isEmailUnique(String email) {
		return !memberRepository.existsByEmail(email);
	}

	public Optional<String> findmemberIdByNameAndEmail(String name, String email) {
		return memberRepository.findByNameAndEmail(name, email).map(Member::getMemberId);
	}

	public Member findMemberById(String memberId) {
		// 이제 memberRepository의 인스턴스를 사용하여 findByMemberId 메서드를 호출할 수 있습니다.
		return memberRepository.findByMemberId(memberId).orElse(null);
	}

	public void updateMemberInfo(Member updatedMember) {
		// 기존 사용자 정보 찾기
		Optional<Member> existingMember = memberRepository.findByMemberId(updatedMember.getMemberId());

		if (existingMember != null) {
			Member member = existingMember.get();
			// 비밀번호 암호화
			member.setPassword(passwordEncoder.encode(updatedMember.getPassword()));
			// 기타 정보 업데이트
			member.setAddressPostCode(updatedMember.getAddressPostCode());
			member.setAddressRoad(updatedMember.getAddressRoad());
			member.setAddressDetails(updatedMember.getAddressDetails());
			member.setMobile(updatedMember.getMobile());
			member.setBankAccountOwner(updatedMember.getBankAccountOwner());
			member.setRefundBankCode(updatedMember.getRefundBankCode());
			member.setBankAccountNo(updatedMember.getBankAccountNo());
			// 기타 필요한 정보 업데이트
			// DB에 저장
			memberRepository.save(member);
		}
	}

	public boolean setDefaultAddressForMember(String memberId, Long addressId) {
		Member member = memberRepository.findByMemberId(memberId).orElse(null);
		Optional<DeliveryAddress> existingDeliveryAddress = deliveryAddressRepository.findById(addressId);
		if (existingDeliveryAddress != null && member != null) {
			DeliveryAddress deliveryAddress = existingDeliveryAddress.get();
			member.setDefaultAddress(deliveryAddress);
			memberRepository.save(member);
			return true;
		}
		return false;
	}

	public LocalDateTime getLastLoginTime(String memberId) {
		Member member = memberRepository.findByMemberId(memberId).orElse(null);
		return member != null ? member.getLastLoginTime() : null;
	}

	public void updateLastLoginTime(String memberId) {
		Member member = memberRepository.findByMemberId(memberId).orElse(null);
		if (member != null) {
			member.setLastLoginTime(LocalDateTime.now()); // 현재 시간으로 마지막 로그인 시간 설정
			memberRepository.save(member); // 업데이트된 정보 저장
		}
	}


	// MemberService.java
	public Page<Member> getMembersPage(Optional<String> keyword, int page, int size) {
	    Pageable pageable = PageRequest.of(page, size);
	    
	    if (keyword.isPresent()) {
	        // 키워드가 있으면 키워드로 회원 검색 로직 구현 (페이징 처리 포함)
	        return memberRepository.findByKeyword(keyword.get(), pageable);
	    } else {
	        // 없으면 모든 회원 정보 가져오기 (페이징 처리 포함)
	        return memberRepository.findAll(pageable);
	    }
	}

	 public Member getMemberById(String memberId) {
        // memberId를 사용하여 회원 정보를 조회합니다.
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID: " + memberId));
    }


}