package noninoni.dto;

import java.util.Map;

public class GoogleMemberInfo implements OAuth2MemberInfo {

    private String sub;
    private String email;
    private String name;
    // 기타 필요한 필드...

    public GoogleMemberInfo(Map<String, Object> attributes) {
        this.sub = (String) attributes.get("sub");
        this.email = (String) attributes.get("email");
        this.name = (String) attributes.get("name");
        // 기타 필드 초기화...
    }

    @Override
    public String getProviderId() {
        return this.sub;  // Google ID가 provider ID와 같음
    }

    @Override
    public String getProvider() {
        return "google";  // 공급자는 "google"
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    // 기타 필요한 메소드...
}