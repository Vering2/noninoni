package noninoni.config;


import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import lombok.RequiredArgsConstructor;
import noninoni.service.OAuth2MemberService;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
	private final OAuth2MemberService oAuth2MemberService;
	private static final int ONE_MONTH = 2678400;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.httpBasic(basic -> basic.disable()).csrf(csrf -> csrf.disable()).cors(withDefaults())
				.authorizeRequests(requests -> requests
						.requestMatchers("/mypage", "/password-confirmation", "/checkout", "/orders/**", "/reviews/my",
								"/favorites", "/shippingaddress", "/view-review")
						.authenticated().requestMatchers("/admin/**").access("hasRole('ADMIN')").anyRequest()
						.permitAll())
				.formLogin(login -> login.loginPage("/login").successHandler(customAuthenticationSuccessHandler())
						.failureHandler(customAuthenticationFailureHandler()).usernameParameter("memberId")
						.passwordParameter("password"))
				.oauth2Login(login -> login.loginPage("/login").defaultSuccessUrl("/").userInfoEndpoint()
						.userService(oAuth2MemberService).and().successHandler(customAuthenticationSuccessHandler()))
				.rememberMe().rememberMeParameter("remember-me") // Remember-Me 파라미터 이름
				.tokenRepository(persistentTokenRepository()).tokenValiditySeconds(2592000) // Remember-Me 토큰 유효기간 (30일)
				.and().logout(logout -> logout.logoutSuccessHandler(new CustomLogoutSuccessHandler()));
		return httpSecurity.build();
	}
	
	@Bean
    public PersistentTokenRepository persistentTokenRepository() {
        InMemoryTokenRepositoryImpl tokenRepository = new InMemoryTokenRepositoryImpl();
        return tokenRepository;
    }

	@Bean
	public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
		return new CustomAuthenticationSuccessHandler();
	}

	@Bean
	public AuthenticationFailureHandler customAuthenticationFailureHandler() {
		return new CustomAuthenticationFailureHandler();
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}
}