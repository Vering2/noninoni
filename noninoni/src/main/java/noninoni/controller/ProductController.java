package noninoni.controller;

import noninoni.dto.CustomUserDetails;
import noninoni.dto.PaginationGroupInfo;
import noninoni.dto.PaginationInfo;
import noninoni.dto.ProductDTO;
import noninoni.dto.ReviewDto;
import noninoni.dto.SizeDto;
import noninoni.entity.Color;
import noninoni.entity.DeliveryFee;
import noninoni.entity.OrderInfo;
import noninoni.entity.Product;
import noninoni.entity.Product.ProductStatus;
import noninoni.entity.ProductVariant;
import noninoni.entity.Review;
import noninoni.entity.Size;
import noninoni.repository.CategoryRepository;
import noninoni.repository.ColorRepository;
import noninoni.repository.DeliveryFeeRepository;
import noninoni.repository.MainCategoryRepository;
import noninoni.repository.ProductRepository;
import noninoni.repository.ProductVariantRepository;
import noninoni.repository.SizeRepository;
import noninoni.service.ColorService;
import noninoni.service.FavoriteService;
import noninoni.service.OrderInfoService;
import noninoni.service.ProductService;
import noninoni.service.ReviewService;
import noninoni.service.SizeService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ProductController extends BaseController {

	@Autowired
	private ProductService productService;

	@Autowired
	private OrderInfoService orderInfoService;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private ColorService colorService;

	@Autowired
	private SizeService sizeService;

	@Autowired
	private FavoriteService favoriteService;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductVariantRepository productVariantRepository;

	@Autowired
	private ColorRepository colorRepository;

	@Autowired
	private SizeRepository sizeRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private MainCategoryRepository mainCategoryRepository;

	@Autowired
	private DeliveryFeeRepository deliveryFeeRepository;

	@GetMapping("/admin/product/list")
	public String AdminlistProducts(@RequestParam(required = false, name = "category") String category,
			@RequestParam(required = false, name = "keyword") String keyword,
			@RequestParam(defaultValue = "0") int page, // 페이지 번호
			@RequestParam(required = false) Integer stock, @RequestParam(required = false) String status, Model model) {
		if (category == null || category.isEmpty()) {
			category = "ALL";
		}

		// 페이지 요청 객체 생성 (ID 내림차순 정렬 추가)
		PageRequest pageRequest = PageRequest.of(page, 12, Sort.by(Sort.Direction.DESC, "id"));

		Page<Product> productsPage;

		if (stock != null) {
			productsPage = productService.getProductsByStockQuantity(stock, pageRequest);
		} else {
			// 상태, 카테고리, 키워드를 모두 고려한 종합적 검색
			productsPage = productService.getProductsByFilters(category, keyword, status, pageRequest);

		}

		// 페이지 그룹 설정
		int totalPages = productsPage.getTotalPages();

		// 페이지가 전혀 없는 경우 (totalPages가 0인 경우)
		if (totalPages == 0) {
			model.addAttribute("startPage", 0);
			model.addAttribute("endPage", 0);
		} else {
			int groupSize = 5; // 한 그룹당 페이지 번호의 수
			int currentPageGroup = page / groupSize; // 현재 페이지 그룹
			int startPage = currentPageGroup * groupSize;
			int endPage = Math.min((currentPageGroup + 1) * groupSize - 1, totalPages - 1);

			model.addAttribute("startPage", startPage); // 그룹 내 시작 페이지 번호
			model.addAttribute("endPage", endPage); // 그룹 내 종료 페이지 번호
		}

		model.addAttribute("count", productsPage.getTotalElements()); // 전체 제품 수
		model.addAttribute("status", status);
		model.addAttribute("category", category);
		model.addAttribute("keyword", keyword);
		model.addAttribute("products", productsPage.getContent()); // 현재 페이지의 제품 목록
		model.addAttribute("totalPages", totalPages); // 총 페이지 수
		model.addAttribute("currentPage", page); // 현재 페이지 번호
		model.addAttribute("categorys", categoryRepository.findAll());
		return "contents/admin/product_list";
	}

	@GetMapping("/admin/product/register")
	public String showRegisterForm(Model model) throws JsonProcessingException {
		// 색상 및 사이즈 목록을 가져오는 로직
		List<Color> colors = colorService.findAllSortedByNumber();
		List<Size> sizes = sizeService.findAllSortedByNumber();

		// 객체를 JSON 문자열로 변환 (Jackson 사용)
		ObjectMapper mapper = new ObjectMapper();
		String colorsJson = mapper.writeValueAsString(colors);
		String sizesJson = mapper.writeValueAsString(sizes);

		model.addAttribute("colorsJson", colorsJson);
		model.addAttribute("sizesJson", sizesJson);

		model.addAttribute("product", new Product());
		model.addAttribute("categories", categoryRepository.findAll());
		model.addAttribute("mainCategories", mainCategoryRepository.findAll());
		return "contents/admin/product_register";
	}

	@PostMapping("/admin/product/register")
	public String registerProduct(Product product,
			@RequestParam(value = "mainImages[]", required = false) MultipartFile[] mainImages,
			@RequestParam(value = "subImages[]", required = false) MultipartFile[] subImages,
			@RequestParam(value = "colors[]", required = false) Long[] colorIds,
			@RequestParam(value = "sizes[]", required = false) Long[] sizeIds,
			@RequestParam(value = "quantities[]", required = false) Integer[] quantities,
			@RequestParam(value = "additionalPrices[]", required = false) Integer[] additionalPrices)
			throws IOException {

		// 상품 이미지 저장 호출
		if (mainImages != null) {
			List<String> storedMainImageFileNames = new ArrayList<>();
			for (MultipartFile file : mainImages) {
				if (!file.isEmpty()) {
					String fileName = storeFile(file, "C:/uploaded_files/product/");
					storedMainImageFileNames.add(fileName); // 파일명 저장
				}
			}
			String joinedMainImageFileNames = String.join(",", storedMainImageFileNames);
			product.setMainimageUrl(joinedMainImageFileNames);
		}
		if (subImages != null) {

			List<String> storedSubImageFileNames = new ArrayList<>();
			for (MultipartFile file : subImages) {
				if (!file.isEmpty()) {
					// 파일이 비어 있지 않은 경우에만 처리
					// 파일 처리 로직...

					String fileName = storeFile(file, "C:/uploaded_files/product/");
					storedSubImageFileNames.add(fileName); // 파일명 저장
				}
			}
			String joinedSubImageFileNames = String.join(",", storedSubImageFileNames);
			product.setSubimageUrl(joinedSubImageFileNames);
		}
		productRepository.save(product);

		if (colorIds != null && sizeIds != null && quantities != null && additionalPrices != null) {
			for (int i = 0; i < colorIds.length; i++) {
				Long colorId = colorIds[i];
				Long sizeId = sizeIds[i];
				Integer quantity = quantities[i];
				Integer additionalPrice = additionalPrices[i];

				if (colorId != null && sizeId != null && quantity != null) {
					Optional<ProductVariant> existingVariant = productVariantRepository
							.findByProductIdAndColorIdAndSizeId(product.getId(), colorId, sizeId);

					if (existingVariant.isPresent()) {
						// 중복되는 상품 변형이 있으면, 수량 및 추가 금액을 업데이트
						ProductVariant variant = existingVariant.get();
						variant.setStockQuantity(variant.getStockQuantity() + quantity);
						variant.setAdditionalPrice(additionalPrice); // 추가 금액 업데이트
						productVariantRepository.save(variant);
					} else {
						// 중복되는 상품 변형이 없으면, 새로운 상품 변형을 저장
						ProductVariant productVariant = new ProductVariant();
						productVariant.setColor(colorRepository.findById(colorId).orElse(null));
						productVariant.setSize(sizeRepository.findById(sizeId).orElse(null));
						productVariant.setStockQuantity(quantity);
						productVariant.setAdditionalPrice(additionalPrice); // 추가 금액 설정
						productVariant.setProduct(product);
						productVariantRepository.save(productVariant);
					}
				}
			}
		}
		return "redirect:/admin/product/list";
	}

	// 제품 수정 페이지로 이동
	@GetMapping("/admin/product/edit/{id}")
	public String editProduct(@PathVariable("id") Long id, Model model) throws JsonProcessingException {
		// 해당 ID의 제품 정보 가져오기

		Product product = productRepository.findById(id).orElse(null);
		List<ProductVariant> variants = productVariantRepository.findByProductId(id);

		model.addAttribute("product", product);
		model.addAttribute("variants", variants);

		// 색상, 사이즈 목록도 추가
		List<Color> colors = colorService.findAllSortedByNumber();
		List<Size> sizes = sizeService.findAllSortedByNumber();
		// 객체를 JSON 문자열로 변환 (Jackson 사용)
		ObjectMapper mapper = new ObjectMapper();
		String colorsJson = mapper.writeValueAsString(colors);
		String sizesJson = mapper.writeValueAsString(sizes);

		model.addAttribute("colorsJson", colorsJson);
		model.addAttribute("sizesJson", sizesJson);
		model.addAttribute("colors", colors);
		model.addAttribute("sizes", sizes);
		model.addAttribute("categories", categoryRepository.findAll());
		model.addAttribute("mainCategories", mainCategoryRepository.findAll());
		return "contents/admin/editProduct"; // editProduct.html 페이지로 이동
	}

	// 제품 수정 처리

	@PostMapping("/admin/product/edit/{id}")
	public String updateProduct(@PathVariable("id") Long id, @ModelAttribute Product product,
			@RequestParam(value = "mainImages[]", required = false) MultipartFile[] mainImages,
			@RequestParam(value = "subImages[]", required = false) MultipartFile[] subImages,
			@RequestParam(value = "variantIds[]", required = false) Long[] variantIds,
			@RequestParam(value = "colors[]", required = false) Long[] colorIds,
			@RequestParam(value = "sizes[]", required = false) Long[] sizeIds,
			@RequestParam(value = "quantities[]", required = false) Integer[] quantities,
			@RequestParam(value = "additionalPrices[]", required = false) Integer[] additionalPrices)
			throws IOException {
		Optional<Product> productOpt = productRepository.findById(product.getId());
		String mainImageUrlori = null;
		String joinedMainImageFileNames = null;
		if (ProductStatus.DISCONTINUED.equals(product.getStatus())) {
			productService.deleteCartItemandFavoriteByProductId(product.getId());
		}

		if (productOpt.isPresent()) {
			Product productori = productOpt.get();
			mainImageUrlori = productori.getMainimageUrl();
			// 여기서 mainImageUrl를 사용할 수 있습니다.
			// 예를 들어, 모델에 추가하거나 뷰에 표시하는 등의 작업을 할 수 있습니다.
		} else {
			// Product 객체가 존재하지 않는 경우의 처리
			// 예를 들어, 사용자에게 제품이 존재하지 않는다는 메시지를 표시하거나,
			// 오류 페이지로 리다이렉트할 수 있습니다.
		}

		if (mainImages != null) {
			List<String> storedMainImageFileNames = new ArrayList<>();
			for (MultipartFile file : mainImages) {
				if (!file.isEmpty()) {
					// 파일이 비어 있지 않은 경우에만 처리
					// 파일 처리 로직...

					String fileName = storeFile(file, "C:/uploaded_files/product/");

					storedMainImageFileNames.add(fileName); // 파일명 저장

				}
			}
			joinedMainImageFileNames = String.join(",", storedMainImageFileNames);
		}
		if (joinedMainImageFileNames == null) {
			product.setMainimageUrl(mainImageUrlori);

		} else {
			product.addMainImageUrl(mainImageUrlori, joinedMainImageFileNames);
		}

		productService.saveProduct(product);

		// 상품 변형 처리
		if (colorIds != null && sizeIds != null && quantities != null && additionalPrices != null) {
			for (int i = 0; i < colorIds.length; i++) {
				Long variantId = variantIds != null && i < variantIds.length ? variantIds[i] : null;
				ProductVariant variant;
				if (variantId != null) {
					// 기존 옵션 업데이트
					variant = productVariantRepository.findById(variantId).orElse(new ProductVariant());
					variant.setStockQuantity(quantities[i]);
				} else {
					// 새로운 옵션 추가 또는 기존 옵션 업데이트
					Optional<ProductVariant> existingVariant = productVariantRepository
							.findByProductIdAndColorIdAndSizeId(product.getId(), colorIds[i], sizeIds[i]);

					if (existingVariant.isPresent()) {
						variant = existingVariant.get();
						variant.setStockQuantity(variant.getStockQuantity() + quantities[i]);
					} else {
						variant = new ProductVariant();
						variant.setStockQuantity(quantities[i]);
					}
				}
				variant.setAdditionalPrice(additionalPrices[i]); // 추가 금액 설정
				variant.setColor(colorRepository.findById(colorIds[i]).orElse(null));
				variant.setSize(sizeRepository.findById(sizeIds[i]).orElse(null));
				variant.setProduct(product);
				productVariantRepository.save(variant);

			}
		}
		return "redirect:/admin/product/list"; // 수정 후 다시 수정 페이지로 리다이렉트
	}

	@PostMapping("/admin/delete-image")
	public ResponseEntity<?> deleteImage(@RequestBody Map<String, String> payload) {
		String imageUrl = payload.get("imageUrl");
		String productIdStr = payload.get("productId");

		// productId가 문자열로 전달될 것으로 예상됩니다. 이를 Long 타입으로 변환합니다.
		Long productId = null;
		try {
			productId = Long.parseLong(productIdStr);
		} catch (NumberFormatException e) {
			return ResponseEntity.badRequest().body("Invalid product ID");
		}

		productService.deleteImage(imageUrl, productId); // productId를 인자로 추가하여 메소드를 호출합니다.

		return ResponseEntity.ok().build();
	}

	@PostMapping("/admin/product/delete")
	public ResponseEntity<?> deleteProduct(@RequestBody Map<String, Long> payload) {
		Long productId = payload.get("id");
		try {

			productService.deleteProduct(productId); // 상품 삭제 로직을 수행

			return ResponseEntity.ok("상품이 삭제되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("상품 삭제 중 오류가 발생했습니다.");
		}
	}

	@PostMapping("/admin/delete-variant")
	@ResponseBody
	public ResponseEntity<?> deleteVariant(@RequestBody Map<String, Long> payload) {
		Long variantId = payload.get("variantId");
		// 상품 변형 ID를 사용하여 상품 변형 찾기
		Optional<ProductVariant> variantOpt = productVariantRepository.findById(variantId);
		if (variantOpt.isPresent()) {
			// 상품 변형이 존재하면 삭제
			productVariantRepository.delete(variantOpt.get());
			return ResponseEntity.ok().build();
		} else {
			// 상품 변형이 존재하지 않으면 에러 응답
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/product/{id}")
	public String product(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
		// 해당 ID의 제품 정보 가져오기
		Product product = productService.getProductById(id);

		// 제품이 존재하지 않거나 AVAILABLE 상태가 아닌 경우 처리
		if (product == null || product.getStatus() != Product.ProductStatus.AVAILABLE) {
			// 적절한 오류 페이지로 리디렉션하거나 메시지 표시
			model.addAttribute("errorMessage", "제품을 찾을 수 없거나 현재 구매할 수 없는 상태입니다.");
			return "error"; // 오류 페이지 또는 메인 페이지로 리디렉션
		}

		// 기타 모델 설정...

		HttpSession session = request.getSession();
		List<Long> recentViewIds = (List<Long>) session.getAttribute("recentViewIds");
		if (recentViewIds == null) {
			recentViewIds = new ArrayList<>();
		}

		// 중복 제거 로직: 현재 상품 ID가 목록에 이미 있는지 확인
		if (!recentViewIds.contains(id)) {
			recentViewIds.add(0, id); // 가장 최근 본 상품 ID를 목록의 앞부분에 추가
		}

		// 최대 개수 제한 (예: 최대 10개)
		final int MAX_ITEMS = 10;
		if (recentViewIds.size() > MAX_ITEMS) {
			recentViewIds = recentViewIds.subList(0, MAX_ITEMS); // 최근 본 상품 ID 목록을 최대 크기로 제한
		}

		session.setAttribute("recentViewIds", recentViewIds);
		long feeId = 1;
		DeliveryFee deliveryFee = deliveryFeeRepository.getReferenceById(feeId);
		model.addAttribute("deliveryFee", deliveryFee);

		model.addAttribute("categories", categoryRepository.findAll());
		model.addAttribute("product", product);
		Long cartId = (Long) session.getAttribute("cartId");
		model.addAttribute("cartId", cartId);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean isFavorite = false; // 즐겨찾기 여부 초기값

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String memberId = userDetails.getUsername();
			isFavorite = favoriteService.isProductFavoritedByMember(memberId, id);
		}

		model.addAttribute("isFavorite", isFavorite); // 즐겨찾기 여부를 모델에 추가

		return "contents/product"; // editProduct.html 페이지로 이동
	}

	@GetMapping("/recent-products")
	public String recentProducts(@RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
		final int PAGE_SIZE = 12; // 페이지 당 상품 개수
		final int GROUP_SIZE = 5; // 페이지 그룹 크기

		// 세션에서 상품 ID 목록을 가져옴
		List<Long> recentViewIds = (List<Long>) session.getAttribute("recentViewIds");
		if (recentViewIds == null) {
			recentViewIds = new ArrayList<>();
		}

		// 상품 ID를 기반으로 실제 Product 객체 목록을 생성
		List<Product> recentViews = recentViewIds.stream().map(id -> productService.getProductById(id))
				.collect(Collectors.toList());

		// 전체 아이템 수 및 전체 페이지 수 계산
		int totalItems = recentViews.size();
		int totalPages = totalItems > 0 ? (int) Math.ceil((double) totalItems / PAGE_SIZE) : 0;

		// 상품이 없는 경우 페이지 및 페이지 그룹 처리
		if (totalPages == 0) {
			model.addAttribute("products", Collections.emptyList());
			model.addAttribute("totalPages", 0);
			model.addAttribute("currentPage", 0);
			model.addAttribute("startPage", 0);
			model.addAttribute("endPage", 0);
			return "contents/recentProducts";
		}

		// 페이지 그룹 설정
		int currentPageGroup = page / GROUP_SIZE;
		int startPage = currentPageGroup * GROUP_SIZE;
		int endPage = Math.min(startPage + GROUP_SIZE - 1, totalPages - 1);

		// 현재 페이지 아이템
		int fromIndex = page * PAGE_SIZE;
		int toIndex = Math.min(fromIndex + PAGE_SIZE, recentViews.size());
		List<Product> pageItems = recentViews.subList(fromIndex, toIndex);

		model.addAttribute("products", pageItems);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("currentPage", page);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String memberId = userDetails.getUsername(); // 현재 로그인한 사용자의 ID 가져오기
			List<Long> favoriteProductIds = favoriteService.getFavoriteProductIdsByMemberId(memberId);
			model.addAttribute("favoriteProductIds", favoriteProductIds);

		}

		return "contents/recentProducts";
	}

	@GetMapping("/favorites")
	public String showFavorites(@RequestParam(defaultValue = "0") int page, Model model) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String memberId = userDetails.getUsername(); // 현재 로그인한 사용자의 ID 가져오기
			List<Long> favoriteProductIds = favoriteService.getFavoriteProductIdsByMemberId(memberId);
			model.addAttribute("favoriteProductIds", favoriteProductIds);
			// 페이지 요청 객체 생성 (ID 내림차순 정렬 추가)
			PageRequest pageRequest = PageRequest.of(page, 12, Sort.by(Sort.Direction.DESC, "id"));
			Page<Product> productsPage = favoriteService.getFavoriteProductsByMemberId(memberId, pageRequest);
			// 페이지 그룹 설정
			int totalPages = productsPage.getTotalPages();
			// 상품이 없는 경우 페이지 및 페이지 그룹 처리
			if (totalPages == 0) {
				model.addAttribute("products", Collections.emptyList());
				model.addAttribute("totalPages", 0);
				model.addAttribute("currentPage", 0);
				model.addAttribute("startPage", 0);
				model.addAttribute("endPage", 0);
				return "contents/favorites";
			}

			int groupSize = 5; // 한 그룹당 페이지 번호의 수
			int currentPageGroup = page / groupSize; // 현재 페이지 그룹
			int startPage = currentPageGroup * groupSize;
			int endPage = Math.min(startPage + groupSize - 1, totalPages - 1);

			model.addAttribute("startPage", startPage); // 그룹 내 시작 페이지 번호
			model.addAttribute("endPage", endPage); // 그룹 내 종료 페이지 번호
			model.addAttribute("products", productsPage.getContent()); // 현재 페이지의 제품 목록
			model.addAttribute("totalPages", totalPages); // 총 페이지 수
			model.addAttribute("currentPage", page); // 현재 페이지 번호
		}

		return "contents/favorites"; // 'favorites.html' 뷰를 반환
	}

	@GetMapping("/product/list")
	public String listProducts(@RequestParam(required = false, name = "category") String category,
			@RequestParam(required = false, name = "keyword") String keyword,
			@RequestParam(defaultValue = "0") int page, // 페이지 번호
			Model model) {
		if (category == null || category.isEmpty()) {
			category = "ALL";
		}
		// 페이지 요청 객체 생성 (ID 내림차순 정렬 추가)
		PageRequest pageRequest = PageRequest.of(page, 12, Sort.by(Sort.Direction.DESC, "id"));

		Page<Product> productsPage = productService.getProducts(category, keyword, pageRequest);

		// 페이지 그룹 설정
		int totalPages = productsPage.getTotalPages();
		if (totalPages > 0) {
			int groupSize = 5; // 한 그룹당 페이지 번호의 수
			int currentPageGroup = page / groupSize; // 현재 페이지 그룹
			int startPage = currentPageGroup * groupSize;
			int endPage = Math.min((currentPageGroup + 1) * groupSize - 1, totalPages - 1);

			model.addAttribute("startPage", startPage); // 그룹 내 시작 페이지 번호
			model.addAttribute("endPage", endPage); // 그룹 내 종료 페이지 번호
		}

		model.addAttribute("count", productsPage.getTotalElements()); // 전체 제품 수
		model.addAttribute("category", category);
		model.addAttribute("keyword", keyword);
		model.addAttribute("products", productsPage.getContent()); // 현재 페이지의 제품 목록
		model.addAttribute("totalPages", totalPages); // 총 페이지 수
		model.addAttribute("currentPage", page); // 현재 페이지 번호
		model.addAttribute("categorys", categoryRepository.findAll());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String memberId = userDetails.getUsername(); // 현재 로그인한 사용자의 ID 가져오기
			List<Long> favoriteProductIds = favoriteService.getFavoriteProductIdsByMemberId(memberId);
			model.addAttribute("favoriteProductIds", favoriteProductIds);

		}

		return "contents/product_list";
	}

	@GetMapping("/categoryProducts")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getCategoryProducts(@RequestParam("categoryType") String categoryType,
			@RequestParam("page") int page) {

		PageRequest pageRequest = PageRequest.of(page, 4, Sort.by(Sort.Direction.DESC, "id"));
		Page<Product> productsPage = productService.getProductsBymainCategory(categoryType, pageRequest);

		List<Long> productIds = productsPage.getContent().stream().map(Product::getId).collect(Collectors.toList());

		List<Long> favoriteProductIds = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String memberId = userDetails.getUsername();
			favoriteProductIds = favoriteService.getFavoriteProductIdsForProducts(memberId, productIds);
		}

		Map<String, Object> response = new HashMap<>();
		List<ProductDTO> productDTOs = productsPage.getContent().stream().map(this::convertToDto)
				.collect(Collectors.toList());

		response.put("products", productDTOs);
		response.put("favoriteProductIds", favoriteProductIds); // 추가된 부분

		PaginationInfo paginationInfo = new PaginationInfo(productsPage.getNumber(), productsPage.getTotalPages(),
				productsPage.getTotalElements());
		response.put("paginationInfo", paginationInfo);

		PaginationGroupInfo paginationGroupInfo = new PaginationGroupInfo(productsPage.getNumber(),
				productsPage.getTotalPages());
		response.put("paginationGroupInfo", paginationGroupInfo);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/product/{productId}/sizes")
	@ResponseBody
	public ResponseEntity<List<SizeDto>> getSizesByProductIdAndColorId(@PathVariable Long productId,
			@RequestParam Long colorId) {
		List<SizeDto> sizesWithAdditionalPrice = productService.getSizesWithStock(productId, colorId);
		if (sizesWithAdditionalPrice.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(sizesWithAdditionalPrice);
	}

	@GetMapping("/product/{productId}/colors")
	public ResponseEntity<List<Color>> getAvailableColorsByProductId(@PathVariable Long productId) {
		List<Color> colors = productService.getAvailableColorsByProductId(productId);
		colors.sort(Comparator.comparingLong(Color::getNumber));
		return ResponseEntity.ok(colors);
	}

	@GetMapping("/api/product/{id}/reviews")
	@ResponseBody
	public ResponseEntity<?> getReviews(@PathVariable Long id, @RequestParam(defaultValue = "0") int page) {
		List<OrderInfo> orderInfos = orderInfoService.findByProductId(id);
		Page<Review> reviews = reviewService.findByOrderInfo(orderInfos,
				PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "createdDate")));

		List<ReviewDto> reviewDtos = reviews.getContent().stream().map(review -> new ReviewDto(review))
				.collect(Collectors.toList());

		Map<String, Object> response = new HashMap<>();
		// 페이지 그룹 설정
		int groupSize = 5; // 한 그룹당 페이지 번호의 수
		int currentPageGroup = page / groupSize; // 현재 페이지 그룹
		int startPage = currentPageGroup * groupSize;
		int endPage = Math.min((currentPageGroup + 1) * groupSize - 1, reviews.getTotalPages() - 1);

		response.put("reviews", reviewDtos);
		response.put("startPage", startPage);
		response.put("endPage", endPage);
		response.put("currentPage", page);
		response.put("totalPages", reviews.getTotalPages());

		return ResponseEntity.ok(response);
	}

	private ProductDTO convertToDto(Product product) {
		// 여기에서 Product 엔티티를 ProductDTO로 변환하는 로직을 구현합니다.
		// 예를 들면, 모든 필드를 복사하는 간단한 매핑이 될 수 있습니다.
		return new ProductDTO(product.getId(), product.getName(), product.getPrice(), product.getMainCategory(),
				product.getMainimageUrl() // 주의: ProductVariant 엔티티도 DTO로 변환할 필요가 있을 수 있습니다.
		);
	}

}
