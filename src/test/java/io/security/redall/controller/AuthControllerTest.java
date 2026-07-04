package io.security.redall.controller;

import io.security.redall.domain.Role;
import io.security.redall.repository.RoleRepository;
import io.security.redall.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 회원가입 통합 테스트.
 * 실제 스프링 컨텍스트 + H2 를 띄우고 HTTP 요청을 흉내내 끝까지 검증한다.
 * @Transactional 로 각 테스트 후 롤백되어 서로 영향을 주지 않는다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp(){
        // data.sql 이 없거나 롤백된 경우를 대비해 기본 권한 보장
        if (roleRepository.findByAuthority("ROLE_USER").isEmpty()){
            roleRepository.save(Role.builder().authority("ROLE_USER").build());
        }
    }

    @Test
    @DisplayName("회원가입 성공: 200 응답 + DB에 암호화되어 저장")
    void signup_success() throws Exception {
        // given
        Map<String, String> body = Map.of(
                "username", "hong123",
                "password", "password123",
                "email", "hong@mail.com",
                "name", "홍길동"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.userId").exists());

        // DB 검증 : 저장 O 비밀번호 평문 X
        var saved = userRepository.findByUsername("hong123").orElseThrow();

        assertThat(saved.getEmail()).isEqualTo("hong@mail.com");
        assertThat(saved.getPassword()).isNotEqualTo("password123");
        assertThat(saved.getPassword()).startsWith("$2a$");
        assertThat(saved.isEnabled()).isFalse();          // 이메일 인증 전
        assertThat(saved.getRoles()).hasSize(1);          // ROLE_USER 부여됨

    }

    @Test
    @DisplayName("아이디 중복 시 400 응답")
    void signup_duplicateUsername() throws Exception {
        // given : 가입
        Map<String, String> first = Map.of(
                "username", "hong123", "password", "password123",
                "email", "hong@mail.com", "name", "홍길동"
        );

        mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isOk());

        // when & then : 같은 아이디로 재가입
        Map<String, String> dup = Map.of(
                "username", "hong123", "password", "other12345",
                "email", "other@mail.com", "name", "김철수");

        mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dup)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."));
    }

    @Test
    @DisplayName("이메일 형식이 틀리면 400 + 검증 메시지")
    void signup_invalidEmail() throws Exception {
        // given : 이메일이 형식에 안 맞음
        Map<String, String> body = Map.of(
                "username", "hong123", "password", "password123",
                "email", "not-an-email", "name", "홍길동"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("필수값 누락(아이디 빈값) 시 400")
    void signup_blankUsername() throws Exception {
        // given
        Map<String, String> body = Map.of(
                "username", "", "password", "password123",
                "email", "hong@mail.com", "name", "홍길동");

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }





}
