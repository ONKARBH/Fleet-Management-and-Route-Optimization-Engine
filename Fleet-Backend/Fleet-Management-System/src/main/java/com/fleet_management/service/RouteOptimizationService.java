// service/optimization/RouteOptimizationService.java
package com.fleet_management.service;

import com.fleet.dto.Coordinate;
import com.fleet_management.dto.OptimizedRoute;
import com.fleet_management.entity.DeliveryTask;
import com.fleet.service.external.OSRMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.*;

@Service
public class RouteOptimizationService {

    @Autowired
    private OSRMService osrmService;

    /**
     * Greedy Nearest Neighbor Algorithm for TSP
     * Complexity: O(n²)
     */
    public Mono<List<Integer>> greedyTSP(double[][] distanceMatrix, int startNode) {
        return Mono.fromCallable(() -> {
            int n = distanceMatrix.length;
            boolean[] visited = new boolean[n];
            List<Integer> path = new ArrayList<>();

            int currentNode = startNode;
            path.add(currentNode);
            visited[currentNode] = true;

            for (int step = 1; step < n; step++) {
                int nextNode = -1;
                double minDistance = Double.MAX_VALUE;

                for (int candidate = 0; candidate < n; candidate++) {
                    if (!visited[candidate] && distanceMatrix[currentNode][candidate] < minDistance) {
                        minDistance = distanceMatrix[currentNode][candidate];
                        nextNode = candidate;
                    }
                }

                if (nextNode != -1) {
                    path.add(nextNode);
                    visited[nextNode] = true;
                    currentNode = nextNode;
                }
            }

            return path;
        });
    }

    /**
     * 2-OPT Algorithm for TSP Improvement
     * Reduces path crossing and improves route quality
     */
    public Mono<List<Integer>> twoOptTSP(double[][] distanceMatrix, List<Integer> initialPath) {
        return Mono.fromCallable(() -> {
            List<Integer> bestPath = new ArrayList<>(initialPath);
            boolean improved = true;

            while (improved) {
                improved = false;
                for (int i = 1; i < bestPath.size() - 1; i++) {
                    for (int j = i + 1; j < bestPath.size(); j++) {
                        double oldDistance = distanceMatrix[bestPath.get(i-1)][bestPath.get(i)] +
                                distanceMatrix[bestPath.get(j)][bestPath.get(j-1)];
                        double newDistance = distanceMatrix[bestPath.get(i-1)][bestPath.get(j-1)] +
                                distanceMatrix[bestPath.get(i)][bestPath.get(j)];

                        if (newDistance < oldDistance) {
                            reverse(bestPath, i, j-1);
                            improved = true;
                        }
                    }
                }
            }
            return bestPath;
        });
    }

    private void reverse(List<Integer> path, int start, int end) {
        while (start < end) {
            int temp = path.get(start);
            path.set(start, path.get(end));
            path.set(end, temp);
            start++;
            end--;
        }
    }

    /**
     * Main optimization method
     */
    public Mono<OptimizedRoute> optimizeRoute(List<DeliveryTask> deliveries, Long startDepotId) {
        if (deliveries == null || deliveries.isEmpty()) {
            return Mono.error(new IllegalArgumentException("No deliveries to optimize"));
        }

        // Convert deliveries to coordinates
        List<Coordinate> coordinates = deliveries.stream()
                .map(d -> new Coordinate(d.getLatitude(), d.getLongitude()))
                .toList();

        // Add depot/warehouse as start point (assuming depot at (0,0) or pass from config)
        List<Coordinate> allPoints = new ArrayList<>();
        allPoints.add(new Coordinate(0.0, 0.0)); // Depot
        allPoints.addAll(coordinates);

        // Get distance matrix from external API
        return osrmService.getDistanceMatrix(allPoints)
                .flatMap(distanceMatrix -> {
                    // Run TSP algorithm
                    return greedyTSP(distanceMatrix, 0) // Start from depot
                            .flatMap(greedyPath -> twoOptTSP(distanceMatrix, greedyPath))
                            .map(optimizedPath -> {
                                // Convert indices back to deliveries
                                List<DeliveryTask> optimizedSequence = new ArrayList<>();
                                double totalDistance = 0;

                                for (int i = 1; i < optimizedPath.size(); i++) {
                                    int deliveryIndex = optimizedPath.get(i) - 1;
                                    if (deliveryIndex >= 0 && deliveryIndex < deliveries.size()) {
                                        optimizedSequence.add(deliveries.get(deliveryIndex));
                                    }

                                    if (i > 0) {
                                        totalDistance += distanceMatrix[optimizedPath.get(i-1)][optimizedPath.get(i)];
                                    }
                                }

                                // Set sequence order
                                for (int i = 0; i < optimizedSequence.size(); i++) {
                                    optimizedSequence.get(i).setSequenceOrder(i + 1);
                                }

                                return OptimizedRoute.builder()
                                        .optimizedSequence(optimizedSequence)
                                        .totalDistance(totalDistance)
                                        .totalDuration(totalDistance / 50.0) // Assuming average speed 50 km/h
                                        .distanceMatrix(convertToDoubleList(distanceMatrix))
                                        .build();
                            });
                });
    }

    private List<List<Double>> convertToDoubleList(double[][] matrix) {
        List<List<Double>> list = new ArrayList<>();
        for (double[] row : matrix) {
            List<Double> rowList = new ArrayList<>();
            for (double val : row) {
                rowList.add(val);
            }
            list.add(rowList);
        }
        return list;
    }
}