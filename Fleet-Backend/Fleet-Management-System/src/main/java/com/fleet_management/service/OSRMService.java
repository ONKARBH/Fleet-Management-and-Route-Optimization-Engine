// service/external/OSRMService.java (Using OpenStreetMap Routing Machine - Free)
package com.fleet.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.dto.Coordinate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
public class OSRMService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${osrm.api.url:http://router.project-osrm.org}")
    private String osrmApiUrl;

    public OSRMService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<Double> getDistanceBetweenPoints(Coordinate from, Coordinate to) {
        String url = String.format("%s/route/v1/driving/%s;%s?overview=false",
                osrmApiUrl, from.toQueryParam(), to.toQueryParam());

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        JsonNode json = objectMapper.readTree(response);
                        JsonNode route = json.get("routes").get(0);
                        double distance = route.get("distance").asDouble();
                        return distance / 1000.0; // Convert to kilometers
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing OSRM response", e);
                    }
                });
    }

    public Mono<double[][]> getDistanceMatrix(List<Coordinate> waypoints) {
        int n = waypoints.size();
        double[][] distances = new double[n][n];

        // Build all pairs of coordinates
        List<Mono<Void>> calls = new java.util.ArrayList<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    final int finalI = i;
                    final int finalJ = j;
                    calls.add(getDistanceBetweenPoints(waypoints.get(i), waypoints.get(j))
                            .doOnNext(distance -> distances[finalI][finalJ] = distance)
                            .then());
                } else {
                    distances[i][j] = 0;
                }
            }
        }

        return Mono.when(calls).thenReturn(distances);
    }
}