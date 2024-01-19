package noninoni.dto;

import lombok.Data;
import noninoni.entity.DeliveryAddress;

@Data
public class DeliveryAddressDTO {

	private DeliveryAddress deliveryAddress;
	private String[] phoneNumber;
	private String[] address;

}
