package io.security.redall.controller;

import io.security.redall.dto.OAuthLoginRequest;
import io.security.redall.service.OAuthLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthLoginService oAuthLoginService;

    @PostMapping("/login")
    public ResponseEntity<?> oauthLogin(@Valid @RequestBody OAuthLoginRequest request){
        Map<String, String> tokens = oAuthLoginService.login(
                request.getProvider(), request.getAccessToken()
        );
        return ResponseEntity.ok(tokens);
    }
}
