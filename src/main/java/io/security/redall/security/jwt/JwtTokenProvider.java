package io.security.redall.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 생성 / 섬증 / 파싱 담당
 * - Access Token : 짧은 수명, 실제 API 인증에 사용
 * - Refresh Token : 긴 수명, Access 재발급에 사용
 *
 * 토큰을 만들고(로그인 성공 시), 토큰이 유효한지 검증하고(매 요청 시), 토큰에서 정보를 꺼내는 역할
 *
 */
@Component
public class JwtTokenProvider {
    private final SecretKey key;

    private final long accessTokenValidity;
    private final long refreshTokenValidity;


    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-token-validity}") long accessTokenValidity,
                            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity) {
        // secret 은 Base64 인코딩된 문자열. 최소 256bit(32byte) 이상이어야 함
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    /** Access Token 생성. subject=username, 권한 정보 포함 */
    public String createAccessToken(String username, String authorities){
        return createToken(username, authorities, accessTokenValidity);
    }

    /** Refresh Token 생성. 권한 없이 username 만 */
    public String createRefreshToken(String username){
        return createToken(username, null, refreshTokenValidity);
    }

    private String createToken(String username, String authorities, long validity){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        JwtBuilder builder = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key);

        if (authorities != null){
            builder.claim("auth", authorities);
        }
        return builder.compact();
    }

    /** 토큰에서 username(subject) 추출 */
    public String getUsername(String token){
        return parseClaims(token).getSubject();
    }

    /** 토큰에서 권한 문자열 추출 */
    public String getAuthorities(String token){
        return parseClaims(token).get("auth", String.class);
    }

    /** 토큰 유효성 검증. 만료/변조/형식오류면 false */
    public boolean validateToken(String token){
        try {
            parseClaims(token);
            return true;
        }catch (ExpiredJwtException e){
            // 만료됨 (재발급 필요)
            return false;
        }catch (JwtException | IllegalArgumentException e){
            // 변조/형식오류/빈 토큰
            return false;
        }
    }

    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
