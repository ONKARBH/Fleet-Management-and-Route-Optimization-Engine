// src/main/java/com/fleet_management/service/OSRMService.java
package com.fleet_management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet_management.dto.Coordinate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;

@Service
public class OSRMService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${osrm.api.url:http://router.project-osrm.org}")
    private String osrmApiUrl;

    @Value("${osrm.use.mock:true}")  // Use mock data for development
    private boolean useMockData;

    public OSRMService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get distance between two points using OSRM API
     */
    public Mono<Double> getDistanceBetweenPoints(Coordinate from, Coordinate to) {
        if (useMockData) {
            return Mono.just(calculateMockDistance(from, to));
        }

        String url = String.format("%s/route/v1/driving/%s;%s?overview=false",
                osrmApiUrl, from.toQueryParam(), to.toQueryParam());

        System.out.println("Calling OSRM API: " + url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        JsonNode json = objectMapper.readTree(response);
                        JsonNode route = json.get("routes").get(0);
                        double distance = route.get("distance").asDouble();
                        return distance / 1000.0; // Convert meters to kilometers
                    } catch (Exception e) {
                        System.err.println("Error parsing OSRM response: " + e.getMessage());
                        return calculateMockDistance(from, to);
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("Error calling OSRM API: " + e.getMessage());
                    return Mono.just(calculateMockDistance(from, to));
                });
    }

    /**
     * Get distance matrix for multiple waypoints
     */
    public Mono<double[][]> getDistanceMatrix(List<Coordinate> waypoints) {
        int n = waypoints.size();
        double[][] distances = new double[n][n];

        if (useMockData) {
            // Generate mock distance matrix
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == j) {
                        distances[i][j] = 0;
                    } else {
                        distances[i][j] = calculateMockDistance(waypoints.get(i), waypoints.get(j));
                    }
                }
            }
            return Mono.just(distances);
        }

        // Real API calls - build matrix by calling distance for each pair
        List<Mono<Void>> calls = new ArrayList<>();

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

    /**
     * Mock distance calculation for development (Haversine formula)
     */
    private double calculateMockDistance(Coordinate from, Coordinate to) {
        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double radius = 6371; // Earth's radius in km
        return radius * c;
    }
}