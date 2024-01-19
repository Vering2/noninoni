package noninoni.dto;

import java.util.Map;

public class KakaoMemberInfo implements OAuth2MemberInfo {
    private String id;
    private String email;
    private String name;

    public KakaoMemberInfo(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.id = String.valueOf(attributes.get("id")); // ID는 attributes의 최상위에 있음

        if (kakaoAccount != null) {
            //this.email = (String) kakaoAccount.get("email"); // 이메일은 kakao_account 내부에 있음
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                this.name = (String) profile.get("nickname"); // 이름(닉네임)은 profile 내부에 있음
            }
        }
    }

    @Override
    public String getProviderId() {
        return this.id;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getEmail() {
        return "kakao@email.com";
    }
}
