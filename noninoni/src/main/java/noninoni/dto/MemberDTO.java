package noninoni.dto;

import lombok.Data;

@Data
public class MemberDTO {

	private String memberId;
	private String password;
	private String name;
	private String addressPostCode;
	private String addressRoad;
	private String addressDetails;
	private String mobile;
	private String email;
	private String bankAccountOwner;
	private String refundBankCode;
	private String bankAccountNo;

	// Getter와 Setter 메서드 추가
}
