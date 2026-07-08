package io.security.redall.center.service;

import io.security.redall.center.domain.BloodCenter;
import io.security.redall.center.dto.BloodCenterPublicData;
import io.security.redall.center.repository.BloodCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 헌혈의 집 ETF 서비스
 * Extract(CSV) -> transform(좌표 변환) => Long(저장)를 조율
 */
@Service
@RequiredArgsConstructor
public class BloodCenterEtlService {

    private final BloodCenterCsvReader csvReader;
    private final KakaoGeocodingClient geocodingClient;
    private final BloodCenterRepository bloodCenterRepository;

    @Transactional
    public EtlResult runEtl(){
        List<BloodCenterPublicData> rawData = csvReader.read();

        int loaded = 0, geocodeFail = 0, duplicate = 0;

        for (BloodCenterPublicData data : rawData){
            Optional<BloodCenter> existing =
                    bloodCenterRepository.findByNameAndAddress(data.name(), data.address());

            if(existing.isPresent()){
                duplicate++;
                continue;
            }

            Optional<KakaoGeocodingClient.Coordinates> coords =
                    geocodingClient.getCoordinates(data.address());

            Double lat = null, lon = null;
            if(coords.isPresent()){
                lat = coords.get().getLat();
                lon = coords.get().getLon();
            }else {
                geocodeFail++;
            }

            BloodCenter bloodCenter = BloodCenter.builder()
                    .bloodBankName(data.bloodBankName())
                    .name(data.name())
                    .code(data.code())
                    .address(data.address())
                    .tel(data.tel())
                    .lat(lat)
                    .lon(lon)
                    .build();

            bloodCenterRepository.save(bloodCenter);
            loaded++;
        }
        return new EtlResult(rawData.size(), loaded, geocodeFail, duplicate);
    }

    public record EtlResult(
            int extracted,  int loaded, int geocodeFail,    int duplicate
    ){}
}
