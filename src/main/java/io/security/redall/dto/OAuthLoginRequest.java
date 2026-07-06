package io.security.redall.dto;

import io.security.redall.oauth.OAuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OAuthLoginRequest {
    @NotNull(message = "provider은 필수입니다.")
    private OAuthProvider provider;

    @NotBlank(message = "accessToken은 필수입니다.")
    private String accessToken;
}
