package noninoni.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import noninoni.dto.CustomUserDetails;
import noninoni.entity.Notice;
import noninoni.service.NoticeService;

import java.util.List;

@Controller
@RequestMapping("/notices")
public class NoticeController {

	private final NoticeService noticeService;

	@Autowired
	public NoticeController(NoticeService noticeService) {
		this.noticeService = noticeService;
	}

	// 공지사항 목록 조회
	@GetMapping
	public String listNotices(Model model, @RequestParam(defaultValue = "0") int page) {
	    final int PAGE_SIZE = 10; // 페이지당 보여줄 아이템 수
	    final int PAGE_GROUP_SIZE = 5; // 페이지 그룹 크기

	    // 상단 고정된 공지사항 조회
	    List<Notice> pinnedNotices = noticeService.findPinnedNotices();

	    // 페이징 처리된 일반 공지사항 조회
	    Page<Notice> noticePage = noticeService.findPaginatedNotices(PageRequest.of(page, PAGE_SIZE));

	    // 페이지 그룹 계산
	    int totalPages = noticePage.getTotalPages();
	    int currentPageGroup = (int) Math.floor((double) page / PAGE_GROUP_SIZE);
	    int startPage = currentPageGroup * PAGE_GROUP_SIZE;
	    int endPage = Math.min(startPage + PAGE_GROUP_SIZE - 1, totalPages - 1);

	    model.addAttribute("pinnedNotices", pinnedNotices);
	    model.addAttribute("noticePage", noticePage);
	    model.addAttribute("startPage", startPage);
	    model.addAttribute("endPage", endPage);
	    model.addAttribute("isAdmin", isAdmin());	    
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("currentPage", page);

	    return "contents/noticeList";
	}


	// 공지사항 상세 조회
	@GetMapping("/{id}")
	public String viewNotice(@PathVariable Long id, Model model) {
		Notice notice = noticeService.findById(id);
		model.addAttribute("notice", notice);
		model.addAttribute("isAdmin", isAdmin());
		return "contents/noticeDetail"; // 공지사항 상세 페이지
	}

	// 공지사항 생성 페이지
	@GetMapping("/create")
	public String createNoticeForm(Model model) {
		if (!isAdmin())
			return "redirect:/notices";
		return "contents/noticeCreate"; // 공지사항 생성 폼 페이지
	}

	// 공지사항 생성 처리
	@PostMapping
	public String createNotice(Notice notice) {
		if (!isAdmin())
			return "redirect:/notices";
		noticeService.save(notice);
		return "redirect:/notices";
	}

	// 공지사항 수정 페이지
	@GetMapping("/{id}/edit")
	public String editNoticeForm(@PathVariable Long id, Model model) {
		Notice notice = noticeService.findById(id);
		model.addAttribute("notice", notice);
		return "contents/noticeEdit"; // 공지사항 수정 폼 페이지
	}

	// 공지사항 수정 처리
	@PostMapping("/{id}/edit")
	public String updateNotice(@PathVariable Long id, Notice notice) {
		noticeService.update(id, notice);
		return "redirect:/notices/" + id;
	}

	// 공지사항 삭제 처리
	@PostMapping("/{id}/delete")
	public String deleteNotice(@PathVariable Long id) {
		noticeService.delete(id);
		return "redirect:/notices";
	}

	// 공지사항 상단 고정
	@PostMapping("/{id}/pin")
	public String pinNotice(@PathVariable Long id) {
		Notice notice = noticeService.findById(id);
		if (notice != null) {
			notice.setPinned(!notice.isPinned()); // 현재 상태를 반전시킵니다.
			noticeService.save(notice);
		}
		return "redirect:/notices";
	}

	public boolean isAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

			// 사용자의 권한 목록에서 'ROLE_ADMIN'을 검색합니다.
			return userDetails.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
		}

		return false; // 로그인하지 않았거나, 사용자 권한이 관리자가 아닌 경우
	}
}
