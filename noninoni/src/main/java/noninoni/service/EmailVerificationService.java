package noninoni.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import noninoni.entity.Member;
import noninoni.repository.MemberRepository;

@Service
public class EmailVerificationService {

	@Autowired
	private MemberRepository memberRepository;

	public String generateToken() {
		return UUID.randomUUID().toString();
	}

	@Autowired
	private AsyncEmailService asyncEmailService;

	public void sendVerificationEmail(String memberEmail) {
		String token = generateToken();
		saveTokenToMember(memberEmail, token);

		String subject = "이메일 인증";
		String body = "인증을 완료하려면 다음 링크를 클릭하세요: https://noninoni.shop/verify?token=" + token + "&email=" + memberEmail;

		// 이메일 전송 로직
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(memberEmail);
		message.setSubject(subject);
		message.setText(body);

		asyncEmailService.sendAsync(message);
	}

	public void saveTokenToMember(String memberEmail, String token) {
		Member member = memberRepository.findByEmail(memberEmail);
		if (member != null) {
			member.setVerificationToken(token);
			memberRepository.save(member);
		}
	}

	public boolean verifyEmail(String memberEmail, String token) {
		Member member = memberRepository.findByEmail(memberEmail);

		if (member != null && token.equals(member.getVerificationToken())) {
			// 토큰이 일치하면 이메일 인증 성공
			member.setEnabled(true);
			memberRepository.save(member);
			return true;
		}

		// 토큰이 일치하지 않으면 이메일 인증 실패
		return false;
	}

}
