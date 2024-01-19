package noninoni.dto;

import java.util.Map;

public class FacebookMemberInfo implements OAuth2MemberInfo{
	private String id;
	private String email;
	private String name;
	// 기타 필요한 필드...

	    public FacebookMemberInfo(Map<String, Object> attributes) {
	        this.id = (String) attributes.get("id");
	        this.email = (String) attributes.get("email");
	        this.name = (String) attributes.get("name");
	        // 기타 필드 초기화...
	    }

	    @Override
	    public String getProviderId() {
	        return this.id;  //
	    }

	    @Override
	    public String getProvider() {
	        return "google";  // 
	    }

	    @Override
	    public String getName() {
	        return this.name;
	    }

	    @Override
	    public String getEmail() {
	        return this.email;
	    }
}