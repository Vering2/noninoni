package noninoni.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;
import noninoni.service.ColorService;
import noninoni.service.DeliveryFeeService;
import noninoni.service.SizeService;

@Slf4j
@Controller
public class ManagementController {

	@Autowired
	private ColorService colorService; // 색상 서비스
	@Autowired
	private SizeService sizeService; // 사이즈 서비스
	@Autowired
	private DeliveryFeeService feeService; // 배송비 서비스

	@GetMapping("/admin/management")
	public String showColors(Model model) {
		model.addAttribute("colors", colorService.findAllSortedByNumber());
		model.addAttribute("sizes", sizeService.findAllSortedByNumber());
		model.addAttribute("fee", feeService.findbyId(1));
		return "contents/admin/management";
	}

	@PostMapping("/admin/colors/add")
	public String addColor(@RequestParam String colorName, @RequestParam long colorNumber) {
		colorService.addColor(colorName, colorNumber);
		return "redirect:/admin/management";
	}

	@GetMapping("/admin/colors/delete/{id}")
	public String deleteColor(@PathVariable Long id) {
		colorService.deleteColor(id);
		return "redirect:/admin/management";
	}

	@PostMapping("/admin/colors/update/{id}")
	public String updateColor(@PathVariable Long id, @RequestParam String name, @RequestParam Long number) {
		colorService.updateColor(id, name, number);
		return "redirect:/admin/management";
	}

	@PostMapping("/admin/sizes/add")
	public String addSize(@RequestParam String sizeName,  @RequestParam long sizeNumber) {
		sizeService.addSize(sizeName, sizeNumber);
		return "redirect:/admin/management";
	}

	@GetMapping("/admin/sizes/delete/{id}")
	public String deleteSize(@PathVariable Long id) {
		sizeService.deleteSize(id);
		return "redirect:/admin/management";
	}

	@PostMapping("/admin/sizes/update/{id}")
	public String updateSize(@PathVariable Long id, @RequestParam String name, @RequestParam Long number) {
		sizeService.updateSize(id, name, number);
		return "redirect:/admin/management";
	}

	@PostMapping("/admin/deliveryFees/update")
	public String updateDeliveryFee(@RequestParam Long id, @RequestParam BigDecimal fee,
			@RequestParam BigDecimal minimum) {
		feeService.updateDeliveryFee(id, fee, minimum);
		return "redirect:/admin/management"; // 관리 페이지로 리다이렉트
	}

	// 사이즈 및 배송비 관리도 유사하게 구현
}
