package noninoni.entity;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Table(name = "members")
public class Member {

	@Id
	private String memberId;
	private String password;
	private String name;
	private String addressDetails;
	private String addressRoad;
	private String addressPostCode;
	private String mobile;
	private String email;
	private String bankAccountOwner;
	private String refundBankCode;
	private String bankAccountNo;
	private String roles;
	private boolean enabled;
	private String verificationToken;
	private String resetToken;
	private String provider; // 공급자 (google, facebook ...)
	private String providerId; // 공급 아이디
	private LocalDateTime lastLoginTime; // 마지막 로그인 시간

	@OneToOne(mappedBy = "member")
	@JsonBackReference
	private Cart cart;

	@OneToOne
	@JoinColumn(name = "default_address_id")
	private DeliveryAddress defaultAddress; // 기본 배송지

	public void setBaseAddressId(DeliveryAddress deliveryAddress) {
		// TODO Auto-generated method stub
		this.defaultAddress = deliveryAddress;

	}

	@Builder
	public Member(String memberId, boolean enabled, String name, String password, String email, String roles,
			String provider, String providerId) {
		this.memberId = memberId;
		this.password = password;
		this.enabled = enabled;
		this.email = email;
		this.roles = roles;
		this.provider = provider;
		this.providerId = providerId;
		this.name = name;
	}

	public Member() {
		this.roles = "ROLE_USER";
	}

	// 비밀번호를 인코딩하여 설정하는 메서드
	public void setPasswordEncoded(PasswordEncoder passwordEncoder, String rawPassword) {
		this.password = passwordEncoder.encode(rawPassword);
	}

	public void setLastLoginTime(LocalDateTime lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

}