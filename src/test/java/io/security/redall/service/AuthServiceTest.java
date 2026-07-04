package io.security.redall.service;

import io.security.redall.domain.Role;
import io.security.redall.domain.User;
import io.security.redall.dto.SignupRequest;
import io.security.redall.repository.RoleRepository;
import io.security.redall.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * AuthService 단위 테스트
 * Repository / PasswordEncoder 를 Mock 으로 대체하여 DB 없이 로직으로만 검증
 */

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private SignupRequest newRequest(){
        SignupRequest req = new SignupRequest();

        // SignupRequest 는 setter 가 없으므로 리플렉션으로 값 주입 (테스트 편의)
        setField(req, "username", "hong123");
        setField(req, "password", "password123!");
        setField(req, "email", "hong@email.com");
        setField(req, "name", "홍길동");

        return req;
    }

    private void setField(Object target, String name, Object value){
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("정상 회원가입: 비밀번호 암호화 후 저장되고 기본 권한이 부여된다")
    void signup_success(){
        // given
        SignupRequest req = newRequest();
        given(userRepository.existsByUsername("hong123")).willReturn(false);
        given(userRepository.existsByEmail("hong@email.com")).willReturn(false);
        given(passwordEncoder.encode("password123!")).willReturn("$2a$encoded");
        given(roleRepository.findByAuthority("ROLE_USER"))
                .willReturn(Optional.of(Role.builder().authority("ROLE_USER").build()));

        // save 시 id 가 부여된 것처럼 흉내
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User u = invocation.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 1L);
            return u;
        });

        // when
        Long userId = authService.signup(req);

        // then
        assertThat(userId).isEqualTo(1L);

        // 비밀번호가 암호화되어 저장됐는지 확인
        then(passwordEncoder).should().encode("password123!");
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("아이디가 중복되면 예외 발생")
    void signup_duplicateUsername(){
        // given
        SignupRequest req = newRequest();
        given(userRepository.existsByUsername("hong123")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 아이디입니다.");

        // 중복이면 저장 x
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("이메일이 중복되면 예외가 발생한다")
    void signup_duplicateEmail(){
        // given
        SignupRequest req = newRequest();
        given(userRepository.existsByUsername("hong123")).willReturn(false);
        given(userRepository.existsByEmail("hong@email.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        // 중복이면 저장 x
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("기본 권한(ROLE_USER)이 없으면 예외가 발생한다")
    void signup_noDefaultRole(){
        // given
        SignupRequest req = newRequest();
        given(userRepository.existsByUsername("hong123")).willReturn(false);
        given(userRepository.existsByEmail("hong@email.com")).willReturn(false);
        given(passwordEncoder.encode("password123!")).willReturn("$2a$encoded");
        given(roleRepository.findByAuthority("ROLE_USER")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.signup(req))
                .isInstanceOf(IllegalStateException.class);

    }


}
