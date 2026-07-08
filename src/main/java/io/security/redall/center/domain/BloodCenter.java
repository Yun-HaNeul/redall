package io.security.redall.center.domain;

import io.security.redall.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "blood_centers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BloodCenter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 관할 혈액원 이름*/
    @Column(length = 100)
    private String bloodBankName;

    /** 헌혈의 집 이름 */
    @Column(nullable = false, length = 200)
    private String name;

    /** 구분 (원내/자체/국고) */
    @Column(length = 50)
    private String code;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 50)
    private String tel;

    //좌표는 변환 실패 시 null 가능
    @Column
    private Double lat;

    @Column
    private Double lon;

    @Builder
    public BloodCenter(String bloodBankName, String name, String code,
                        String address, String tel,
                       Double lat, Double lon){
        this.bloodBankName = bloodBankName;
        this.name = name;
        this.code = code;
        this.address = address;
        this.tel = tel;
        this.lat = lat;
        this.lon = lon;
    }

    public void updateCoordinates(Double lat, Double lon){
        this.lat = lat;
        this.lon = lon;
    }

    public boolean hasCoordinates(){
        return lat != null && lon != null;
    }
}
