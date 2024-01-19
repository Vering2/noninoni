package noninoni.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import noninoni.entity.Member;

public interface MemberRepository extends JpaRepository<Member, String> {

	// 아이디로 회원을 찾는 메서드
	boolean existsByMemberId(String memberId);

	// 회원 아이디로 회원 조회
	Optional<Member> findByMemberId(String memberId);

	Member findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByMemberIdAndNameAndEmail(String memberId, String name, String email);

	Optional<Member> findByNameAndEmail(String name, String email);

	Member findByMemberIdAndEmail(String memberId, String email);

	@Query("SELECT m FROM Member m WHERE "
			+ "CONCAT(m.memberId, ' ', m.name, ' ', m.email, ' ', m.mobile, ' ', m.addressDetails, ' ', m.addressRoad, ' ', m.addressPostCode, ' ', m.bankAccountOwner, ' ', m.refundBankCode, ' ', m.bankAccountNo, ' ', m.roles, ' ', m.provider, ' ', m.providerId) LIKE %:keyword%")
	Page<Member> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

}