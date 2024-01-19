package noninoni.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import noninoni.dto.CustomUserDetails;
import noninoni.service.FavoriteService;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteApiController {

	@Autowired
	private FavoriteService favoriteService;

	@PostMapping("/add")
	public ResponseEntity<?> addFavorite(@RequestParam String memberId, @RequestParam Long productId) {
		favoriteService.addFavorite(memberId, productId);
		return ResponseEntity.ok("Added to favorites");
	}

	@PostMapping("/remove")
	public ResponseEntity<?> removeFavorite(@RequestParam String memberId, @RequestParam Long productId) {
		favoriteService.removeFavorite(memberId, productId);
		return ResponseEntity.ok("Removed from favorites");
	}

	@GetMapping("/count")
	public ResponseEntity<?> getFavoriteCount(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
			return ResponseEntity.ok(0); // 사용자가 로그인하지 않은 경우 0 반환
		}
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		String memberId = userDetails.getUsername();
		int favoriteCount = favoriteService.getFavoriteProductIdsByMemberId(memberId).size();
		return ResponseEntity.ok(favoriteCount);
	}
}
