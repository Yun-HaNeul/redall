package io.security.redall.security;
import io.security.redall.domain.User;
import io.security.redall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인 시 Security 가 호출한다.
 * username 으로 회원을 찾아 CustomUserDetails 로 감싸 반환하면,
 * Security 가 입력 비밀번호와 대조하고 계정 상태(잠금/활성 등)를 검사한다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRoles(username)   // ← 변경
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다: " + username));
        return new CustomUserDetails(user);
    }
}
