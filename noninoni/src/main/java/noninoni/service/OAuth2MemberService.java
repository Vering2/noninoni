package noninoni.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import noninoni.dto.CustomUserDetails;
import noninoni.dto.FacebookMemberInfo;
import noninoni.dto.GoogleMemberInfo;
import noninoni.dto.KakaoMemberInfo;
import noninoni.dto.NaverMemberInfo;
import noninoni.dto.OAuth2MemberInfo;
import noninoni.entity.Member;
import noninoni.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class OAuth2MemberService extends DefaultOAuth2UserService {
	private final PasswordEncoder encoder;	
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2MemberInfo memberInfo = null;
        
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            memberInfo = new GoogleMemberInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
            memberInfo = new FacebookMemberInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            memberInfo = new KakaoMemberInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            memberInfo = new NaverMemberInfo(oAuth2User.getAttributes());
        } else {
        }
        
        String provider = memberInfo.getProvider();
        String providerId = memberInfo.getProviderId();
        String name = memberInfo.getName();
        String memberId = provider + "_" + providerId; //중복이 발생하지 않도록 provider와 providerId를 조합
        String email = memberInfo.getEmail();
        Optional<Member> findMember = memberRepository.findByMemberId(memberId);
        
        Member member;
        if (findMember.isPresent()) {
            member = findMember.get();
        } else {
            member = Member.builder()
                    .memberId(memberId)
                    .email(email)
                    .name(name)
                    .enabled(true)
                    .password(encoder.encode("password"))
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            memberRepository.save(member);
        }
        return new CustomUserDetails(member, oAuth2User.getAttributes());
    }
}