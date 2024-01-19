package noninoni.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import noninoni.entity.Member;
import noninoni.repository.MemberRepository;
import noninoni.service.PasswordResetService;

@Controller
public class PasswordResetController extends BaseController {

	@Autowired
	private PasswordResetService passwordResetService;

	@Autowired
	private MemberRepository memberRepository;

	// 비밀번호 리셋 요청 페이지 보여주기
	@GetMapping("/forgot-password")
	public String showForgotPasswordForm(Model model) {

		return "contents/forgot-password";
	}

	// 비밀번호 리셋 요청 처리
	@PostMapping("/forgot-password")
	public String handleForgotPassword(@RequestParam("email") String email, @RequestParam("memberId") String memberId) {
		Member member = memberRepository.findByMemberIdAndEmail(memberId, email);

		if (member != null) {
			passwordResetService.requestPasswordReset(member);
			return "redirect:/forgot-password?success"; // 비밀번호 리셋 이메일 전송 성공 시 리다이렉트
		} else {
			return "redirect:/forgot-password?error"; // 이메일이 존재하지 않을 경우 리다이렉트
		}

	}

	// 비밀번호 리셋 페이지 보여주기
	@GetMapping("/reset-password")
	public String showResetPasswordForm(@RequestParam("token") String token, @RequestParam("email") String email, @RequestParam("memberId") String memberId,
			Model model) {
		model.addAttribute("token", token);
		model.addAttribute("email", email);
		model.addAttribute("memberId", memberId);

		return "contents/reset-password";
	}

	// 비밀번호 리셋 처리
	@PostMapping("/reset-password")
	public String handleResetPassword(@RequestParam("token") String token, @RequestParam("email") String email, @RequestParam("memberId") String memberId,
			@RequestParam("password") String password) {
		if (passwordResetService.verifyResetToken(email, memberId, token)) {
			passwordResetService.resetPassword(email, memberId, password);
			return "redirect:/login"; // 비밀번호 리셋 성공 시 로그인 페이지로 리다이렉트
		} else {
			return "redirect:/forgot-password"; // 토큰 검증 실패 시 에러 페이지로 리다이렉트
		}
	}
}