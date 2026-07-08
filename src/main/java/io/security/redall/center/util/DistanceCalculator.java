package io.security.redall.center.util;

/**
 * 두 좌효 사이 거리 계산 (하버사인 공식)
 * 지구가 구라는 것을 감안해 위경도 사이의 실제 거리(km)를 구함
 */
public class DistanceCalculator {
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * 두 좌표 사이 거리를 km로 반환
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2){

        // 위도/경도를 라디안으로 변환
        double rlat1 = Math.toRadians(lat1);
        double rlat2 = Math.toRadians(lat2);
        double dlat = Math.toRadians(lat2 - lat1);
        double dlon = Math.toRadians(lon2 - lon1);

        // 하버사인 공식
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(rlat1) * Math.cos(rlat2)
                * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));

        return EARTH_RADIUS_KM * c;
    }
}
