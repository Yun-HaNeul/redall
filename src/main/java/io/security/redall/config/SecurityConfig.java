package io.security.redall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    /**
     * 비밀번호 암호화
     * 회원가입 시 평문 비밀번호를 해시해서 저장한 후,
     * 로그인 시 입력값과 저장된 해시를 대조
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 개발 단계: 일단 모든 요청 허용
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // H2 콘솔은 CSRF 예외
                .csrf(csrf -> csrf.disable())
                // H2 콘솔이 iframe로 뜨므로 frameOptions 허용
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }
}
