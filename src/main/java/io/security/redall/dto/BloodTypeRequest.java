package io.security.redall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 혈액형 등록/수정 요청
 */
@Getter
@NoArgsConstructor
public class BloodTypeRequest {
    @NotBlank(message = "혈액형은 필수입니다.")
    private String bloodType;

    @NotBlank(message = "RH 타입은 필수입니다.")
    private String rhType;
}
