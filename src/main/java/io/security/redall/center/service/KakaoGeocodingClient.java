package io.security.redall.center.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 카카오 지오코딩: 주소 -> 좌표(위경도) 변환 (Transform 단계)
 */
@Component
public class KakaoGeocodingClient {
    private static final String GEOCODE_URI = "https://dapi.kakao.com/v2/local/search/address.json";

    private final RestClient restClient = RestClient.create();

    @Value("${oauth.kakao.client-id}")
    private String kakaoRestApiKey;

    @SuppressWarnings("unchecked")
    public Optional<Coordinates> getCoordinates(String address){
        try {
            Map<String, Object> response = restClient.get()
                    .uri(GEOCODE_URI + "?query=" + address)
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return Optional.empty();

            List<Map<String, Object>> documents =
                    (List<Map<String, Object>>) response.get("documents");

            if(documents == null || documents.isEmpty()){
                return Optional.empty();
            }

            Map<String, Object> first = documents.get(0);
            double lon = Double.parseDouble((String) first.get("x"));
            double lat = Double.parseDouble((String) first.get("y"));

            return Optional.of(new Coordinates(lat, lon));
        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Getter
    public static class Coordinates {
        private final double lat;
        private final double lon;

        public Coordinates(double lat, double lon){
            this.lat = lat;
            this.lon = lon;
        }
    }
}
