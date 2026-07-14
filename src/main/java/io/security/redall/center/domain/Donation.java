package io.security.redall.center.domain;

import io.security.redall.domain.BaseTimeEntity;
import io.security.redall.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 헌혈 기록
 * 사용자가 직접 입력하는 개인 데이터
 *
 * 장소는 둘 중 하나:
 * - bloodCenter:   헌혈의 집에서 한 경우 (지도에 표시 가능)
 * - placeName:     직접 입력 (헌혈버스, 단체헌혈 등)
 */
@Getter
@Entity
@Table(name = "donations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Donation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 기록 소유자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 헌혈일 */
    @Column(nullable = false)
    private LocalDate donationDate;

    /** 헌혈 종류 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DonationType donationType;

    /** 헌혈의 집 (목록에서 선택한 경우) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_center_id")
    private BloodCenter bloodCenter;

    /** 직접 입력한 장소 (헌혈버스 등) */
    @Column(length = 200)
    private String placeName;

    /** 메모 */
    @Column(length = 500)
    private String memo;

    @Builder
    public Donation(User user, LocalDate donationDate, DonationType donationType,
                    BloodCenter bloodCenter, String placeName, String memo) {
        this.user = user;
        this.donationDate = donationDate;
        this.donationType = donationType;
        this.bloodCenter = bloodCenter;
        this.placeName = placeName;
        this.memo = memo;
    }

    /** 수정  */
    public void update(LocalDate donationDate, DonationType donationType, BloodCenter bloodCenter, String placeName, String memo) {
        this.donationDate = donationDate;
        this.donationType = donationType;
        this.bloodCenter = bloodCenter;
        this.placeName = placeName;
        this.memo = memo;
    }

    /** 장소 이름 (헌혈의 집이면 해당 이름, 아니면 직접 입력한 값) */
    public String getPlaceDisplayName(){
        if(bloodCenter != null){
            return bloodCenter.getName();
        }
        return placeName != null ? placeName : "미지정";
    }

    /** 본인 기록인지 확인 (권한 체크용)  */
    public boolean inOwnedBy(Long userId){
        return this.user.getId().equals(userId);
    }

}
