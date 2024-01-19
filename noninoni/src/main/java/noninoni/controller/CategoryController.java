package noninoni.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;
import noninoni.entity.Category;
import noninoni.entity.MainCategory;
import noninoni.entity.Product;
import noninoni.repository.CategoryRepository;
import noninoni.repository.MainCategoryRepository;
import noninoni.repository.ProductRepository;

@Slf4j
@Controller
public class CategoryController extends BaseController {

	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private ProductRepository productRepository; 

	@Autowired
	private MainCategoryRepository mainCategoryRepository;

	// 카테고리 목록 조회
	@GetMapping("/admin/category")
	public String listCategories(Model model) {

		model.addAttribute("categories", categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "number")));
		return "contents/admin/category";
	}

	// 카테고리 추가
	@PostMapping("/admin/category/add")
	public String addCategory(@RequestParam("type") String type, @RequestParam("number") Long number,
			RedirectAttributes redirectAttributes) {
		List<Category> existingCategories = categoryRepository.findByType(type);
		if (!existingCategories.isEmpty()) {
			// 중복된 type이 존재하는 경우
			redirectAttributes.addFlashAttribute("error", "해당 카테고리는 이미 존재합니다.");
			return "redirect:/admin/category";
		}

		Category newCategory = new Category();
		newCategory.setType(type);
		newCategory.setNumber(number);
		categoryRepository.save(newCategory);
		return "redirect:/admin/category";
	}

	// 카테고리 삭제
	@PostMapping("/admin/category/delete")
	public String deleteCategory(@RequestParam("id") Long id) {
		categoryRepository.deleteById(id);
		return "redirect:/admin/category";
	}

	// 카테고리 업데이트
	@PostMapping("/admin/category/update")
	public String updateCategory(@RequestParam("id") Long id, @RequestParam("type") String newType,
			@RequestParam("number") Long number) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + id));
		String oldType = category.getType();
		category.setType(newType);
		category.setNumber(number);
		categoryRepository.save(category);

		// 상품 테이블 업데이트
		if (!oldType.equals(newType)) {
			List<Product> productsToUpdate = productRepository.findByCategory(oldType);
			for (Product product : productsToUpdate) {
				product.setCategory(newType);
			}
			productRepository.saveAll(productsToUpdate);
		}

		return "redirect:/admin/category";
	}

	// 카테고리 목록 조회
	@GetMapping("/admin/mainCategory")
	public String listMainCategories(Model model) {

		model.addAttribute("categories", mainCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "number")));
		return "contents/admin/mainCategory";
	}

	// 카테고리 추가
	@PostMapping("/admin/mainCategory/add")
	public String addMainCategory(@RequestParam("type") String type, @RequestParam("number") Long number,
			RedirectAttributes redirectAttributes) {
		List<MainCategory> existingCategories = mainCategoryRepository.findByType(type);
		if (!existingCategories.isEmpty()) {
			// 중복된 type이 존재하는 경우
			redirectAttributes.addFlashAttribute("error", "해당 카테고리는 이미 존재합니다.");
			return "redirect:/admin/mainCategory";
		}

		MainCategory MainCategory = new MainCategory();
		MainCategory.setType(type);
		MainCategory.setNumber(number);
		mainCategoryRepository.save(MainCategory);
		return "redirect:/admin/mainCategory";
	}

	// 카테고리 삭제
	@PostMapping("/admin/mainCategory/delete")
	public String deleteMainCategory(@RequestParam("id") Long id) {
		mainCategoryRepository.deleteById(id);
		return "redirect:/admin/mainCategory";
	}

	// 카테고리 업데이트
	@PostMapping("/admin/mainCategory/update")
	public String updateMainCategory(@RequestParam("id") Long id, @RequestParam("type") String newType,
			@RequestParam("number") Long number) {
		MainCategory mainCategory = mainCategoryRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid mainCategory ID: " + id));
		String oldType = mainCategory.getType();
		mainCategory.setType(newType);
		mainCategory.setNumber(number);
		mainCategoryRepository.save(mainCategory);
		
		// 상품 테이블 업데이트
		if (!oldType.equals(newType)) {
			List<Product> productsToUpdate = productRepository.findByMainCategory(oldType);
			for (Product product : productsToUpdate) {
				product.setMainCategory(newType);
			}
			productRepository.saveAll(productsToUpdate);
		}
		
		return "redirect:/admin/mainCategory";
	}
}
