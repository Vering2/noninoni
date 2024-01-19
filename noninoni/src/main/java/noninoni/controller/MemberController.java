package noninoni.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import noninoni.dto.MemberDTO;
import noninoni.entity.Member;
import noninoni.repository.MemberRepository;
import noninoni.service.MemberService;

@Slf4j
@RestController
public class MemberController {

	@Autowired
	private MemberService memberService;

	

	@GetMapping("/admin/members/details/{memberId}")
	@ResponseBody
	public Member getMemberDetails(@PathVariable String memberId) {
		return memberService.getMemberById(memberId);
	}

	// 아이디 중복 확인 컨트롤러
	@PostMapping("/checkDuplicateId")
	public ResponseEntity<?> checkDuplicateId(MemberDTO memberDTO) {
		boolean isDuplicate = memberService.checkDuplicateId(memberDTO.getMemberId());

		Map<String, Object> response = new HashMap<>();
		response.put("duplicate", isDuplicate);
		return ResponseEntity.ok(response);

	}

	@PostMapping("/checkEmailUnique")
	public ResponseEntity<Boolean> checkEmailUnique(MemberDTO memberDTO) {
		boolean isUnique = memberService.isEmailUnique(memberDTO.getEmail());
		return ResponseEntity.ok(isUnique);
	}

	

	@Autowired
	private MemberRepository memberRepository;

	@PostMapping("/validate-input")
	public ResponseEntity<?> validateInput(@RequestBody MemberDTO input) {
		boolean isValid = memberRepository.existsByMemberIdAndNameAndEmail(input.getMemberId(), input.getName(),
				input.getEmail());

		return ResponseEntity.ok(Collections.singletonMap("isValid", isValid));
	}

	@PostMapping("/find-id")
	public ResponseEntity<?> findId(@RequestBody Map<String, String> payload) {
		String name = payload.get("name");
		String email = payload.get("email");
		Optional<String> memberId = memberService.findmemberIdByNameAndEmail(name, email);
		if (memberId.isPresent()) {
			return ResponseEntity.ok(Collections.singletonMap("message", "회원님의 아이디는 " + memberId.get() + "입니다."));
		} else {
			return ResponseEntity.ok(Collections.singletonMap("message", "일치하는 사용자가 없습니다."));
		}
	}

}
