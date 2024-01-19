package noninoni.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String basePath = "file:///C:/uploaded_files/";

		registry.addResourceHandler("/uploaded_files/**").addResourceLocations(basePath).setCachePeriod(31536000);

		registry.addResourceHandler("/.well-known/acme-challenge/**")
				.addResourceLocations(basePath + ".well-known/acme-challenge/");

	}
}
