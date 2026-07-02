package io.security.redall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing  // BaseTimeEntity 의 created_at/last_updated_at 자동 기록
@SpringBootApplication
public class RedallApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedallApplication.class, args);
    }

}
