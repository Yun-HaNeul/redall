package io.security.redall.center.dto;

import io.security.redall.center.domain.DonationType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 헌혈 기록 등록/수정 요청
 * 장소는 bloodCenterId 또는 placeName 중 하나
 */
@Getter
@NoArgsConstructor
public class DonationRequest {

    @NotNull(message = "헌혈일은 필수입니다.")
    private LocalDate donationDate;

    @NotNull(message = "헌혈 종류는 필수입니다.")
    private DonationType donationType;

    private Long bloodCenterId;     //  헌혈의 집에서 한 경우
    private String placeName;       //  직접 입력한 경우
    private String memo;
}
