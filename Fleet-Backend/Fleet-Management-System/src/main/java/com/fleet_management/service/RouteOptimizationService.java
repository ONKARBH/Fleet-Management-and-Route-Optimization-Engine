// src/main/java/com/fleet_management/service/RouteOptimizationService.java
package com.fleet_management.service;

import com.fleet_management.dto.Coordinate;
import com.fleet_management.dto.OptimizedRoute;
import com.fleet_management.entity.DeliveryTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.*;

@Service
public class RouteOptimizationService {

    @Autowired
    private OSRMService osrmService;

    /**
     * Optimize route using Nearest Neighbor algorithm (Greedy)
     * Time Complexity: O(n²)
     */
    public Mono<OptimizedRoute> optimizeRouteNearestNeighbor(List<DeliveryTask> deliveries, Coordinate startPoint) {
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(startPoint);
        for (DeliveryTask task : deliveries) {
            coordinates.add(new Coordinate(task.getLatitude(), task.getLongitude()));
        }

        return osrmService.getDistanceMatrix(coordinates)
                .map(distanceMatrix -> {
                    List<Integer> optimizedOrder = nearestNeighborTSP(distanceMatrix);
                    return buildOptimizedRoute(deliveries, optimizedOrder, distanceMatrix);
                });
    }

    /**
     * Nearest Neighbor algorithm for TSP
     */
    private List<Integer> nearestNeighborTSP(double[][] distanceMatrix) {
        int n = distanceMatrix.length;
        boolean[] visited = new boolean[n];
        List<Integer> path = new ArrayList<>();

        // Start from depot (index 0)
        int current = 0;
        path.add(current);
        visited[current] = true;

        for (int step = 1; step < n; step++) {
            int next = -1;
            double minDistance = Double.MAX_VALUE;

            for (int candidate = 0; candidate < n; candidate++) {
                if (!visited[candidate] && distanceMatrix[current][candidate] < minDistance) {
                    minDistance = distanceMatrix[current][candidate];
                    next = candidate;
                }
            }

            if (next != -1) {
                path.add(next);
                visited[next] = true;
                current = next;
            }
        }

        return path;
    }

    /**
     * Optimize route using 2-OPT algorithm (improves Nearest Neighbor)
     */
    public Mono<OptimizedRoute> optimizeRouteWith2OPT(List<DeliveryTask> deliveries, Coordinate startPoint) {
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(startPoint);
        for (DeliveryTask task : deliveries) {
            coordinates.add(new Coordinate(task.getLatitude(), task.getLongitude()));
        }

        return osrmService.getDistanceMatrix(coordinates)
                .map(distanceMatrix -> {
                    List<Integer> initialPath = nearestNeighborTSP(distanceMatrix);
                    List<Integer> optimizedPath = twoOptOptimization(distanceMatrix, initialPath);
                    return buildOptimizedRoute(deliveries, optimizedPath, distanceMatrix);
                });
    }

    /**
     * 2-OPT algorithm for TSP optimization
     * Reduces path crossing and improves route quality
     */
    private List<Integer> twoOptOptimization(double[][] distanceMatrix, List<Integer> path) {
        List<Integer> bestPath = new ArrayList<>(path);
        boolean improved = true;
        int iterations = 0;
        int maxIterations = 100;

        while (improved && iterations < maxIterations) {
            improved = false;
            iterations++;

            for (int i = 1; i < bestPath.size() - 1; i++) {
                for (int j = i + 1; j < bestPath.size(); j++) {
                    double currentDistance = calculatePathDistance(distanceMatrix, bestPath);
                    swap(bestPath, i, j);
                    double newDistance = calculatePathDistance(distanceMatrix, bestPath);

                    if (newDistance < currentDistance) {
                        improved = true;
                    } else {
                        // Swap back if not improved
                        swap(bestPath, i, j);
                    }
                }
            }
        }

        return bestPath;
    }

    private void swap(List<Integer> path, int i, int j) {
        while (i < j) {
            int temp = path.get(i);
            path.set(i, path.get(j));
            path.set(j, temp);
            i++;
            j--;
        }
    }

    private double calculatePathDistance(double[][] distanceMatrix, List<Integer> path) {
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            total += distanceMatrix[path.get(i)][path.get(i + 1)];
        }
        return total;
    }

    /**
     * Build optimized route object from path
     */
    private OptimizedRoute buildOptimizedRoute(List<DeliveryTask> deliveries,
                                               List<Integer> path,
                                               double[][] distanceMatrix) {
        List<DeliveryTask> optimizedSequence = new ArrayList<>();
        List<Integer> optimizedIndices = new ArrayList<>();
        double totalDistance = 0;
        double totalDuration = 0;

        // Skip first point (depot) and map to actual deliveries
        for (int i = 1; i < path.size(); i++) {
            int deliveryIndex = path.get(i) - 1;
            if (deliveryIndex >= 0 && deliveryIndex < deliveries.size()) {
                DeliveryTask task = deliveries.get(deliveryIndex);
                task.setSequenceOrder(i);
                optimizedSequence.add(task);
                optimizedIndices.add(deliveryIndex);
            }

            if (i > 0) {
                totalDistance += distanceMatrix[path.get(i-1)][path.get(i)];
                totalDuration += distanceMatrix[path.get(i-1)][path.get(i)] / 50.0; // 50 km/h avg speed
            }
        }

        return OptimizedRoute.builder()
                .optimizedSequence(optimizedSequence)
                .optimizedOrderIndices(optimizedIndices)
                .totalDistance(totalDistance)
                .totalDuration(totalDuration)
                .totalFuelCost(totalDistance * 12.0)
                .totalTimeHours(totalDuration)
                .distanceMatrix(convertToDoubleList(distanceMatrix))
                .build();
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