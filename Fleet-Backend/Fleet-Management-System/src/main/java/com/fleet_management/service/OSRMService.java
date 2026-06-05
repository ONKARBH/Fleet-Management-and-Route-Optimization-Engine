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

    public OSRMService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<Double> getDistanceBetweenPoints(Coordinate from, Coordinate to) {
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
                        return distance / 1000.0; // Convert to kilometers
                    } catch (Exception e) {
                        System.err.println("Error parsing OSRM response: " + e.getMessage());
                        throw new RuntimeException("Error parsing OSRM response", e);
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("Error calling OSRM API: " + e.getMessage());
                    // Return a default distance for development
                    return Mono.just(calculateEuclideanDistance(from, to));
                });
    }

    private double calculateEuclideanDistance(Coordinate from, Coordinate to) {
        // Simple Euclidean distance for development when OSRM is not available
        double latDiff = from.getLatitude() - to.getLatitude();
        double lonDiff = from.getLongitude() - to.getLongitude();
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff) * 111; // Rough conversion to km
    }

    public Mono<double[][]> getDistanceMatrix(List<Coordinate> waypoints) {
        int n = waypoints.size();
        double[][] distances = new double[n][n];

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
}