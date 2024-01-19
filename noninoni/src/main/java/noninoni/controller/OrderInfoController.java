package noninoni.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import noninoni.entity.OrderInfo;
import noninoni.service.OrderInfoService;

@RestController
public class OrderInfoController {

	@Autowired
	private OrderInfoService orderInfoService;

	@PostMapping("/confirm-purchase/{orderInfoId}")
	public ResponseEntity<?> confirmPurchase(@PathVariable Long orderInfoId) {
		try {
			OrderInfo orderInfo = orderInfoService.findById(orderInfoId);
			if (orderInfo == null) {
				return ResponseEntity.notFound().build();
			}

			orderInfo.setOrderConfirm(true);
			orderInfoService.save(orderInfo);

			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
		}
	}
}
