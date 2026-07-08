package io.security.redall.center.dto;

/**
 * CSV에서 읽은 헌혈의집 원본 데이터 1건 (Extract 단계)
 */
public record BloodCenterPublicData(
        String bloodBankName,
        String name,
        String code,
        String address,
        String tel

) {
    public boolean isValid(){
        return name != null && !name.isBlank()
                && address != null && !address.isBlank();
    }
}
