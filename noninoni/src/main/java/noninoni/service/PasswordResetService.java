package noninoni.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import noninoni.entity.Member;
import noninoni.repository.MemberRepository;

//비밀번호 찾기 서비스
@Service
public class PasswordResetService {

	@Autowired
	private MemberRepository memberRepository;
	
	

	public String generateToken() {
		return UUID.randomUUID().toString();
	}

	@Autowired
    private AsyncEmailService asyncEmailService;

	public void requestPasswordReset(Member member) {

		if (member != null) {
			// 비밀번호 리셋 토큰 생성 및 저장
			String resetToken = generateToken();
			member.setResetToken(resetToken);
			memberRepository.save(member);

			// 이메일에 비밀번호 리셋 링크 포함하여 전송
			String resetLink = "https://noninoni.shop/reset-password?token=" + resetToken + "&email=" + member.getEmail() + "&memberId=" + member.getMemberId();
			String subject = "비밀번호 리셋";
			String body = "비밀번호를 재설정하려면 다음 링크를 클릭하세요: " + resetLink;

			// 이메일 전송 로직
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(member.getEmail());
			message.setSubject(subject);
			message.setText(body);

			 asyncEmailService.sendAsync(message);
		}
	}

	public boolean verifyResetToken(String email, String memberId, String resetToken ) {
		Member member = memberRepository.findByMemberIdAndEmail(memberId, email);


		if (member != null && resetToken.equals(member.getResetToken())) {
			// 토큰이 일치하면 비밀번호 리셋 가능
			return true;
		}

		// 토큰이 일치하지 않거나 사용자가 없으면 비밀번호 리셋 불가
		return false;
	}
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	public void resetPassword(String email, String memberId,String newPassword) {
		Member member = memberRepository.findByMemberIdAndEmail(memberId, email);

		if (member != null) {
			// 비밀번호 리셋
			member.setPasswordEncoded(passwordEncoder, newPassword);
			member.setResetToken(null); // 토큰 초기화 또는 삭제
			memberRepository.save(member);
		}
	}
}