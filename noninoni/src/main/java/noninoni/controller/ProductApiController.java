package noninoni.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class ProductApiController {

    @GetMapping("/recent-products/count")
    public ResponseEntity<Integer> getRecentProductsCount(HttpSession session) {
        List<Long> recentViewIds = (List<Long>) session.getAttribute("recentViewIds");
        if (recentViewIds == null) {
            return ResponseEntity.ok(0);
        }
        return ResponseEntity.ok(recentViewIds.size());
    }
}