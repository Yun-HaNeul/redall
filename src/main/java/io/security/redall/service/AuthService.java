package io.security.redall.service;

import io.security.redall.domain.Role;
import io.security.redall.domain.User;
import io.security.redall.dto.SignupRequest;
import io.security.redall.repository.RoleRepository;
import io.security.redall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 일반 회원가입
     * @return 저장된 회원의 id
     */
    @Transactional
    public Long signup(SignupRequest request){
        // 1. 아이디 중복 검사
        if (userRepository.existsByUsername(request.getUsername())){
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 2. 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 3. 비밀번호 암호화 ( 평문 저장 금지)
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. 회원 생성 ( 가입 직후 enabled=false, 이메일 인증 전)
        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .email(request.getEmail())
                .name(request.getName())
                .build();

        // 5. 기본 권한(ROLE_USER) 설정
        Role userRole = roleRepository.findByAuthority("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("기본 권한이 없습니다. data.sql를 확인하세요."));

        user.addRole(userRole);

        // 6. 저장
        userRepository.save(user);

        return user.getId();
    }

}
