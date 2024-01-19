package noninoni.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import noninoni.entity.Member;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails, OAuth2User {
	private final Member member;
	private final Map<String, Object> attributes;

	public CustomUserDetails(Member member, Map<String, Object> attributes) {
		this.member = member;
		this.attributes = attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Arrays.stream(member.getRoles().split(",")).map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
		// 권한 목록 반환
	}

	@Override
	public String getPassword() {
		// 비밀번호 반환
		return member.getPassword();
	}

	@Override
	public String getUsername() {
		// 사용자 이름 반환
		return member.getMemberId();
	}

	@Override
	public boolean isAccountNonExpired() {
		// 계정 만료 여부 반환
		return true; // 또는 실제 상태에 맞게 반환
	}

	@Override
	public boolean isAccountNonLocked() {
		// 계정 잠김 여부 반환
		return true; // 또는 실제 상태에 맞게 반환
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// 자격 증명 만료 여부 반환
		return true; // 또는 실제 상태에 맞게 반환
	}

	@Override
	public boolean isEnabled() {
		// 계정 활성화 여부 반환
		return member.isEnabled();
	}

	// UserDetails와 OAuth2User의 메소드 구현

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public String getName() {
		return member.getName();
	}

	// 기타 UserDetails 메소드 구현
	
	public String getProvider() {
		return member.getProvider();
	}
}
