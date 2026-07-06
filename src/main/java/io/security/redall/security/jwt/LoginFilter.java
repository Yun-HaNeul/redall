package io.security.redall.security.jwt;

import io.security.redall.security.CustomUserDetails;
import io.security.redall.service.LoginAttemptService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final ObjectMapper objectMapper;

    public LoginFilter(AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider, LoginAttemptService loginAttemptService, ObjectMapper objectMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAttemptService = loginAttemptService;
        this.objectMapper = objectMapper;
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {
        try {
            Map<String, String> body = objectMapper.readValue(
                    request.getInputStream(), Map.class);
            String username = body.get("username");
            String password = body.get("password");

            // 실패 핸들러에서 쓸 수 있또록 아이디를 요청에 저장
            request.setAttribute("attemptedUsername", username);

            // 인증 시도 전: 잠김 계정이 10분이 지났으면 자동 해제
            loginAttemptService.unlockIfExpired(username);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // 필드로 주입받은 매니저를 직접 사용 (getAuthenticationManager() 쓰지 않음)
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
        String username = userDetails.getUsername();

        // 로그인 성공: 실패 카운트 리셋
        loginAttemptService.handleSuccess(username);

        String authorities = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.joining(","));

        String accessToken = jwtTokenProvider.createAccessToken(username, authorities);
        String refreshToken = jwtTokenProvider.createRefreshToken(username);

        boolean mustChangePassword = userDetails.getUser().isTempPassword();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "mustChangePassword", mustChangePassword
        ));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException {
        // 실패 원인에 따라 메시지 구분
        String message;

        if (failed instanceof LockedException) {
            message = "계정이 잠겼습니다. 잠시 후 다시 시도하거나 비밀번호 찾기를 이용하세요.";
        } else if (failed instanceof DisabledException) {
            message = "이메일 인증이 완료되지 않은 계쩡입니다.";
        } else {
            // 비밀번호 불일치 / 없는 아이디 -> 실패 카운트 증가
            // 실패한 아이디를 요청에서 다시 꺼내 카운트 증가
            String username = obtainFailedUsername(request);

            if (username != null) {
                loginAttemptService.handleFailure(username);
            }

            message = "로그인에 실패했습니다. 아이디 또는 비밀번호를 확인하세요.";
        }


        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of(
                "message", message
        ));
    }

    /**
     * 실패 시점에는 인증 객체가 없으므로, attemptAuthentication 에서
     * 저장해둔 아이디를 request 속성에서 꺼낸다.
     *
     * @param request
     * @return
     */
    private String obtainFailedUsername(HttpServletRequest request) {
        Object username = request.getAttribute("attemptedUsername");
        return username != null ? username.toString() : null;
    }
}