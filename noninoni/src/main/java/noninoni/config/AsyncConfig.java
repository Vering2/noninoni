package noninoni.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean
    public ExecutorService executorService() {
        // 필요한 스레드 풀의 크기에 따라 숫자를 조정할 수 있습니다.
        return Executors.newFixedThreadPool(10);
    }
}