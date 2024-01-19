package noninoni.controller;

import noninoni.entity.Member;
import noninoni.entity.Order;
import noninoni.entity.OrderInfo;
import noninoni.entity.Review;
import noninoni.service.OrderInfoService;
import noninoni.service.OrderService;
import noninoni.service.ProductService;
import noninoni.service.ReviewService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ReviewController extends BaseController {

	@Autowired
	private OrderInfoService orderInfoService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private ReviewService reviewService;

	// 리뷰 수정 페이지로 이동
	@RequestMapping("/edit-review/{reviewId}")
	public String editReview(@PathVariable("reviewId") Long reviewId, Model model) {
		Review review = reviewService.getReviewById(reviewId); // 리뷰 정보 조회

		// 현재 인증된 사용자의 정보를 가져옴
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName(); // 현재 로그인한 사용자의 이름 (또는 ID)
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
		// 리뷰 작성자와 현재 로그인한 사용자가 일치하는지 확인
		if (isAdmin || review.getOrderInfo().getOrder().getMember().getMemberId().equals(currentUsername)) {
			model.addAttribute("review", review);
			return "contents/updateReview"; // 리뷰 수정 페이지로 이동
		} else {
			// 권한이 없는 경우
			return "redirect:/reviews/my"; // 접근 거부 페이지 또는 적절한 메시지 페이지로 리디렉션
		}
	}

	@PostMapping("/update-review")
	public String updateReview(@RequestParam Long reviewId, @RequestParam String title, @RequestParam String text,
			@RequestParam Integer rating,
			@RequestParam(value = "newImageUrl[]", required = false) MultipartFile[] newImages,
			@RequestParam(value = "deleteImages", required = false) String deleteImageUrls, HttpServletRequest request)
			throws IOException {

		// 현재 로그인한 사용자 확인
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentMemberId = authentication.getName();

		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

		// 리뷰 조회
		Review review = reviewService.getReviewById(reviewId);
		if (!isAdmin && (review == null
				|| !review.getOrderInfo().getOrder().getMember().getMemberId().equals(currentMemberId))) {
			return "redirect:/error";
		}

		// 관리자가 아닌 경우, 주문 상태 확인 및 리뷰 숨김 여부 확인
		if (!isAdmin && (review.getOrderInfo().getOrderStatus().toString().equals("CANCELED") || review.isHidden())) {
			return "redirect:/error";
		}

		// 현재 이미지 URL 목록 가져오기
		List<String> currentImageUrls = new ArrayList<>();
		String existingImageUrls = review.getImageUrl();
		if (existingImageUrls != null && !existingImageUrls.isEmpty()) {
			currentImageUrls.addAll(Arrays.asList(existingImageUrls.split(",")));
		}

		// 삭제할 이미지 URL 제거
		if (deleteImageUrls != null && !deleteImageUrls.isEmpty()) {
			String[] urlsToDelete = deleteImageUrls.split(",");
			for (String fileName : urlsToDelete) {
				// 파일 시스템에서 파일 존재 여부 확인
				File file = new File("C:/uploaded_files/review/" + fileName);
				if (file.exists()) {
					file.delete(); // 파일이 존재하면 삭제
				}
				// DB에서의 이미지 URL 제거
				currentImageUrls.remove(fileName); // 목록에서 해당 이미지 URL 제거
			}
		}

		if (newImages != null) {
			for (MultipartFile file : newImages) {
				if (!file.isEmpty()) {
					String fileName = storeFile(file, "C:/uploaded_files/review/"); // 파일
					currentImageUrls.add(fileName);
				}
			}
		}

		// 이미지 URL 업데이트
		String updatedImageUrls = String.join(",", currentImageUrls); // 수정된 이미지 URL 목록
		review.setImageUrl(updatedImageUrls); // 리뷰 객체에 업데이트된 URL 목록 설정

		// 리뷰 업데이트
		review.setTitleName(title);
		review.setText(text);
		review.setRating(rating);

		reviewService.updateReview(review); // 리뷰 정보 저장

		// 리뷰 수정 후 리디렉션할 페이지 지정
		return "redirect:/reviews/my";
	}

	@PostMapping("/admin/toggle-review-hidden-status")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public String toggleReviewHiddenStatus(@RequestParam("reviewId") Long reviewId,
			RedirectAttributes redirectAttributes) {
		try {
			reviewService.toggleReviewHiddenStatus(reviewId);
			redirectAttributes.addFlashAttribute("successMessage", "리뷰 상태가 변경되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "리뷰 상태 변경 중 오류가 발생했습니다.");
		}
		return "redirect:/reviews"; // 리뷰 목록 페이지로 리디렉션
	}

	@GetMapping("/reviews")
	public String listReviews(Model model, @RequestParam(defaultValue = "0") int page,
			@RequestParam(required = false) String searchTerm) {
		Page<Review> reviews;

		if (searchTerm != null && !searchTerm.trim().isEmpty()) {
			// 검색어를 사용하여 OrderInfo 검색
			List<OrderInfo> orderInfos = orderInfoService.findByProductNameContaining(searchTerm);
			reviews = reviewService.findByOrderInfo(orderInfos,
					PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdDate")));
		} else {
			// 검색어가 없는 경우 모든 리뷰를 조회
			reviews = reviewService.findAll(PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdDate")));
		}

		// 페이지 그룹 설정
		int totalPages = reviews.getTotalPages();
		if (totalPages > 0) {
			int groupSize = 5; // 한 그룹당 페이지 번호의 수
			int currentPageGroup = page / groupSize; // 현재 페이지 그룹
			int startPage = currentPageGroup * groupSize;
			int endPage = Math.min((currentPageGroup + 1) * groupSize - 1, totalPages - 1);

			model.addAttribute("startPage", startPage); // 그룹 내 시작 페이지 번호
			model.addAttribute("endPage", endPage); // 그룹 내 종료 페이지 번호
			model.addAttribute("totalPages", totalPages); // 총 페이지 수
			model.addAttribute("currentPage", page); // 현재 페이지 번호 추가
			model.addAttribute("reviews", reviews);
		} else {
			model.addAttribute("reviews", Collections.emptyList());
			model.addAttribute("totalPages", 0);
			model.addAttribute("currentPage", 0);
			model.addAttribute("startPage", 0);
			model.addAttribute("endPage", 0);
		}
		model.addAttribute("searchTerm", searchTerm); // 검색어 추가

		return "contents/reviewList";
	}

	@GetMapping("/reviews/my")
	public String listUserReviews(Model model, @RequestParam(defaultValue = "0") int page) {
		// 현재 로그인한 사용자의 식별자를 가져옵니다.
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentMemberId = authentication.getName(); // 현재 로그인한 사용자의 ID

		// 해당 사용자의 모든 주문을 조회합니다.
		List<Order> orders = orderService.getAllOrdersByMemberId(currentMemberId);

		// 주문 정보를 수집합니다.
		List<OrderInfo> orderInfos = new ArrayList<>();
		for (Order order : orders) {
			orderInfos.addAll(order.getOrderInfos());
		}

		// 주문 정보를 기반으로 리뷰를 조회합니다.
		Page<Review> reviews = reviewService.findByOrderInfo(orderInfos,
				PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdDate")));

		// 페이지 그룹 설정
		int totalPages = reviews.getTotalPages();
		if (totalPages > 0) {
			int groupSize = 5; // 한 그룹당 페이지 번호의 수
			int currentPageGroup = page / groupSize; // 현재 페이지 그룹
			int startPage = currentPageGroup * groupSize;
			int endPage = Math.min((currentPageGroup + 1) * groupSize - 1, totalPages - 1);

			model.addAttribute("startPage", startPage); // 그룹 내 시작 페이지 번호
			model.addAttribute("endPage", endPage); // 그룹 내 종료 페이지 번호
			model.addAttribute("totalPages", totalPages); // 총 페이지 수
			model.addAttribute("currentPage", page); // 현재 페이지 번호 추가
			model.addAttribute("reviews", reviews);
		} else {
			model.addAttribute("reviews", Collections.emptyList());
			model.addAttribute("totalPages", 0);
			model.addAttribute("currentPage", 0);
			model.addAttribute("startPage", 0);
			model.addAttribute("endPage", 0);
		}
		return "contents/myreviewList";
	}

	@GetMapping("/write-review/{orderInfoId}")
	public String writeReview(@PathVariable Long orderInfoId, Model model) {
		OrderInfo orderInfo = orderInfoService.findById(orderInfoId);
		if (orderInfo == null || orderInfo.getOrderStatus().toString().equals("CANCELED")) {
			// 주문 정보가 존재하지 않거나 주문 상태가 'CANCELED'인 경우
			return "redirect:/error";
		}

		Order order = orderInfo.getOrder();
		// 현재 로그인한 사용자의 memberId 가져오기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentMemberId = authentication.getName(); // 현재 로그인한 사용자의 ID
		if (!order.getMember().getMemberId().equals(currentMemberId)) {
			// 로그인한 사용자가 주문한 사용자와 일치하지 않는 경우
			return "redirect:/error";
		}

		// 이미 작성된 리뷰가 있는지 확인
		if (reviewService.hasReview(orderInfoId)) {
			// 이미 리뷰가 작성된 경우 다른 페이지로 리디렉션
			return "redirect:/already-reviewed"; // 'already-reviewed'는 이미 리뷰가 작성된 것을 알리는 페이지
		}

		// 주문 정보를 모델에 추가하여 뷰에 전달
		model.addAttribute("orderInfo", orderInfo);

		// 리뷰 작성 폼을 제공하는 뷰의 이름 반환
		return "contents/writeReview";
	}

	@PostMapping("/submit-review")
	public String submitReview(@RequestParam Long orderInfoId, @RequestParam String title, @RequestParam String text,
			@RequestParam Integer rating,
			@RequestParam(value = "imageUrl[]", required = false) MultipartFile[] imageUrl) throws IOException {

		// 현재 로그인한 사용자의 memberId 가져오기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentMemberId = authentication.getName(); // 현재 로그인한 사용자의 ID

		OrderInfo orderInfo = orderInfoService.findById(orderInfoId);
		if (orderInfo == null || !orderInfo.getOrder().getMember().getMemberId().equals(currentMemberId)) {
			// OrderInfo를 찾지 못했거나, 로그인한 사용자가 주문한 사용자와 일치하지 않는 경우
			return "redirect:/error";
		}

		// 이미 작성된 리뷰가 있는지 확인
		if (reviewService.hasReview(orderInfoId)) {
			// 이미 리뷰가 작성된 경우 다른 페이지로 리디렉션
			return "redirect:/orders";
		} else {

			Review review = new Review();
			review.setOrderInfo(orderInfo);
			review.setTitleName(title);
			review.setText(text);
			if (imageUrl != null) {
				List<String> storedimageUrls = new ArrayList<>();
				for (MultipartFile file : imageUrl) {
					if (!file.isEmpty()) {
						// 파일이 비어 있지 않은 경우에만 처리
						// 파일 처리 로직...

						String fileName = storeFile(file, "C:/uploaded_files/review/");

						storedimageUrls.add(fileName); // 파일명 저장
					}
				}
				String joinedimageUrls = String.join(",", storedimageUrls);
				review.setImageUrl(joinedimageUrls);
			}
			review.setRating(rating); // 별점 설정

			Review savedReview = reviewService.save(review); // 저장된 리뷰 객체 반환

			// OrderInfo 엔티티에 리뷰 ID 설정
			orderInfo.setReviewId(savedReview.getId());
			orderInfoService.save(orderInfo); // OrderInfo 엔티티 업데이트

			// 리뷰 제출 후 리디렉션할 페이지 지정
			return "redirect:/orders";
		}

	}

	@GetMapping("/view-review")
	public String viewReview(@RequestParam Long reviewId, Model model) {
		Review review = reviewService.findById(reviewId);
		OrderInfo orderInfo = orderInfoService.findByReviewId(reviewId);
		Order order = orderInfo.getOrder();
		Member member = order.getMember();
		if (review == null) {
			// 리뷰를 찾을 수 없는 경우 적절한 처리
			return "redirect:/error";
		}
		model.addAttribute("member", member);
		model.addAttribute("review", review);

		// 리뷰 상세 정보를 보여주는 뷰의 이름 반환
		return "contents/viewReview"; // 'viewReview'는 리뷰 상세 정보를 보여주는 HTML 템플릿 파일의 이름입니다.
	}

}
