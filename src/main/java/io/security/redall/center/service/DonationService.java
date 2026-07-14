package io.security.redall.center.service;

import io.security.redall.center.domain.BloodCenter;
import io.security.redall.center.domain.Donation;
import io.security.redall.center.domain.DonationType;
import io.security.redall.center.dto.DonationRequest;
import io.security.redall.center.dto.DonationResponse;
import io.security.redall.center.repository.BloodCenterRepository;
import io.security.redall.center.repository.DonationRepository;
import io.security.redall.domain.User;
import io.security.redall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 헌혈 기록 CRUD 서비스
 * 본인 기록만 접근 가능하도록  권한 체크
 */
@Service
@RequiredArgsConstructor
public class DonationService {
    private final DonationRepository donationRepository;
    private final BloodCenterRepository bloodCenterRepository;
    private final UserRepository userRepository;

    /** 헌혈 기록 등록  */
    @Transactional
    public DonationResponse create(String username, DonationRequest request){
        User user = getUser(username);
        BloodCenter center = findBloodCenter(request.getBloodCenterId());

        Donation donation = Donation.builder()
                .user(user)
                .donationDate(request.getDonationDate())
                .donationType(request.getDonationType())
                .bloodCenter(center)
                .placeName(center == null ? request.getPlaceName() : null)
                .memo(request.getMemo())
                .build();

        donationRepository.save(donation);

        return DonationResponse.from(donation, 0, 0);
    }

    /** 내 헌혈 기록 목록 (최신순, 회차 자동 계산) */
    @Transactional(readOnly = true)
    public List<DonationResponse> getMyDonations(String username){
        User user = getUser(username);

        // 오래된 순으로 가져와서 회차 계산
        List<Donation> donations = donationRepository
                .findByUserIdOrderByDonationDateAsc(user.getId());

        // 종류별 카운터
        Map<DonationType, Integer> typeCounter = new HashMap<>();
        List<DonationResponse> result = new ArrayList<>();

        for (int i = 0; i < donations.size(); i++){
            Donation d = donations.get(i);
            DonationType type = d.getDonationType();

            // 종류별 회차 증가
            int typeSeq = typeCounter.merge(type, 1, Integer::sum);
            // 전체 회차 (오래된 것부터)
            int seq = i + 1;

            result.add(DonationResponse.from(d, seq, typeSeq));
        }

        // 화면에는 최신순으로 정렬
        Collections.reverse(result);

        return result;
    }

     /** 수정 (본인 것만) */
     @Transactional
     public DonationResponse update(String username, Long donationId, DonationRequest request){
         User user = getUser(username);
         Donation donation = getOwnedDonation(donationId, user.getId());

         BloodCenter center = findBloodCenter(request.getBloodCenterId());

         donation.update(
                 request.getDonationDate(),
                 request.getDonationType(),
                 center,
                 center == null ? request.getPlaceName() : null,
                 request.getMemo()
         );

         return DonationResponse.from(donation, 0, 0);
     }

     /** 삭제 (본인 것만) */
     @Transactional
     public void delete(String username, Long donationId){
         User user = getUser(username);
         Donation donation = getOwnedDonation(donationId, user.getId());
         donationRepository.delete(donation);
     }

     /** 내 헌혈 요약 (총 횟수, 마지막 헌혈일) */
     @Transactional(readOnly = true)
     public Map<String, Object> getSummary(String username){
         User user = getUser(username);
         Long userId = user.getId();

         long totalCount = donationRepository.countByUserId(userId);
         Optional<Donation> last =
                 donationRepository.findFirstByUserIdOrderByDonationDateDesc(userId);

         Map<String, Object> result = new HashMap<>();

         result.put("totalCount", totalCount);
         result.put("lastDonationDate", last.map(Donation::getDonationDate).orElse(null));
         result.put("lastDonationType", last.map(d -> d.getDonationType().getDisplayName()).orElse(null));

         return result;
     }


    // -- 내부 헬퍼 --
    private Donation getOwnedDonation(Long donationId, Long userId){
         Donation donation = donationRepository.findById(donationId)
                 .orElseThrow(() -> new IllegalArgumentException("헌혈 기록을 찾을 수 없습니다."));

         if(!donation.inOwnedBy(userId)){
             throw new IllegalArgumentException("본인의 기륵만 수정/삭제할 수 있습니다.");
         }

         return donation;
    }

    private User getUser(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private BloodCenter findBloodCenter(Long bloodCenterId){
        if(bloodCenterId == null){
            return null;
        }
        return bloodCenterRepository.findById(bloodCenterId)
                .orElseThrow(() -> new IllegalArgumentException("헌혈의 집을 찾을 수 없습니다."));
    }
}
