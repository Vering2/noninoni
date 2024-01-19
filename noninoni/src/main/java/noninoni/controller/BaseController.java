package noninoni.controller;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import noninoni.dto.CustomUserDetails;
import noninoni.entity.Category;
import noninoni.model.Menu;
import noninoni.repository.CategoryRepository;

@Slf4j
@ControllerAdvice
public abstract class BaseController {
	@Autowired
	private CategoryRepository categoryRepository;

	@ModelAttribute
	protected void addCommonAttributes(Model model) {

		List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "number"));
		List<Menu> typemenus = new ArrayList<>();
		Menu menu;

		for (Category category : categories) {
			// 카테고리의 type을 기반으로 메뉴 생성
			// 메뉴의 URL은 카테고리에 따라 달라질 수 있습니다.
			menu = new Menu(category.getType(), "/product/list?category=" + category.getType());
			typemenus.add(menu);
		}
		model.addAttribute("typemenus", typemenus);

		model.addAttribute("logoimagepath", "/images/noninonifinallogo.png");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			model.addAttribute("userName", userDetails.getName());
			model.addAttribute("memberId", userDetails.getUsername());
			model.addAttribute("userRole", userDetails.getAuthorities().toString());
			boolean isLoggedIn = authentication != null && authentication.isAuthenticated();
			model.addAttribute("isLoggedIn", isLoggedIn);

		}

	}

	protected CustomUserDetails getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof CustomUserDetails) {
				return (CustomUserDetails) principal;
			}
		}
		return null;
	}

	protected BufferedImage resizeImage(BufferedImage originalImage) {
		int originalWidth = originalImage.getWidth();
		int originalHeight = originalImage.getHeight();
		double aspectRatio = (double) originalHeight / originalWidth;
		int targetWidth = 1020;
		int targetHeight = (int) (targetWidth * aspectRatio);

		if (originalWidth > targetWidth || originalHeight > targetHeight) {
			BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics2D = resizedImage.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
			graphics2D.dispose();
			return resizedImage;
		} else {
			return originalImage; // 리사이징이 필요 없으면 원본 이미지 반환
		}
	}

	protected String storeFile(MultipartFile file, String directory) throws IOException {
		// 원본 이미지를 BufferedImage로 읽기
		BufferedImage originalImage = ImageIO.read(file.getInputStream());
		// 리사이징된 이미지 가져오기
		BufferedImage resizedImage = resizeImage(originalImage);

		// 원본 파일명에서 확장자 추출
		String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
		String fileExtension = "";

		int extensionIndex = originalFileName.lastIndexOf(".");
		if (extensionIndex > 0) {
			fileExtension = originalFileName.substring(extensionIndex);
		}

		// 파일 저장 경로 설정
		Path fileStorageLocation = Paths.get(directory).toAbsolutePath().normalize();
		Files.createDirectories(fileStorageLocation);

		// 고유한 파일명 생성
		String newFileName = UUID.randomUUID().toString() + fileExtension;
		Path targetLocation = fileStorageLocation.resolve(newFileName);

		// 파일 저장
		File outputFile = targetLocation.toFile();
		ImageIO.write(resizedImage, "jpg", outputFile);

		return newFileName;
	}

}
