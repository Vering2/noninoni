package noninoni.dto;

import java.util.List;

import lombok.Data;

@Data
public class CartItemDeleteRequest {
	private List<Long> itemIds; // For logged-in users
	private List<String> cartKeys; // For non-logged-in users

	// Getters and setters
}
