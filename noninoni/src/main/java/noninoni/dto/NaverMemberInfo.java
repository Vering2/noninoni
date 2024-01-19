package noninoni.dto;

import java.util.Map;

public class NaverMemberInfo implements OAuth2MemberInfo {
    private String id;
    private String email;
    private String name;

    public NaverMemberInfo(Map<String, Object> attributes) {
        if (attributes.containsKey("response")) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");

            // 유효성 검사와 예외 처리
            this.id = (String) response.getOrDefault("id", "");
            this.email = (String) response.getOrDefault("email", "");
            this.name = (String) response.getOrDefault("name", "");
        }
    }

    @Override
    public String getProviderId() {
        return this.id;
    }

    @Override
    public String getProvider() {
        return "naver";
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
