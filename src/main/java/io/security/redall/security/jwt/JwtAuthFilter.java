package io.security.redall.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 매 요청마다 한 번씩 실행되어 JWT 를 검증하는 필터
 * Authorization 헤더의 "Bearer {token}"을 꺼내 유효하면
 * SecurityContext 에 인증 정보를 넣어 "인증된 사용자"로 만듬
 * 토큰이 없거나 유효하지 않으면 통과 (다음 단계에서 권한 검사로 걸림).
 */
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider){
        this.jwtTokenProvider = jwtTokenProvider;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if(token != null && jwtTokenProvider.validateToken(token)){
            String username = jwtTokenProvider.getUsername(token);
            String authorities = jwtTokenProvider.getAuthorities(token);

            //  토큰 정보로 인증 객체를 만들어 SecurityContext 에 저장
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            AuthorityUtils.commaSeparatedStringToAuthorityList(
                                    authorities == null ? "" : authorities)
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /** Authorization 헤더에서 "Bearer " 를 떼고 토큰만 추출 */
    private String resolveToken(HttpServletRequest request){
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")){
            return bearer.substring(7);
        }
        return null;
    }
}
