// src/main/java/com/fleet_management/dto/OptimizedRoute.java
package com.fleet_management.dto;

import com.fleet_management.entity.DeliveryTask;
import java.util.List;

public class OptimizedRoute {
    private List<DeliveryTask> optimizedSequence;
    private Double totalDistance;
    private Double totalDuration;
    private List<List<Double>> distanceMatrix;

    // Default constructor
    public OptimizedRoute() {
    }

    // All-args constructor
    public OptimizedRoute(List<DeliveryTask> optimizedSequence, Double totalDistance,
                          Double totalDuration, List<List<Double>> distanceMatrix) {
        this.optimizedSequence = optimizedSequence;
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
        this.distanceMatrix = distanceMatrix;
    }

    // Getters and Setters
    public List<DeliveryTask> getOptimizedSequence() {
        return optimizedSequence;
    }

    public void setOptimizedSequence(List<DeliveryTask> optimizedSequence) {
        this.optimizedSequence = optimizedSequence;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Double getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Double totalDuration) {
        this.totalDuration = totalDuration;
    }

    public List<List<Double>> getDistanceMatrix() {
        return distanceMatrix;
    }

    public void setDistanceMatrix(List<List<Double>> distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    // Builder pattern implementation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<DeliveryTask> optimizedSequence;
        private Double totalDistance;
        private Double totalDuration;
        private List<List<Double>> distanceMatrix;

        public Builder optimizedSequence(List<DeliveryTask> optimizedSequence) {
            this.optimizedSequence = optimizedSequence;
            return this;
        }

        public Builder totalDistance(Double totalDistance) {
            this.totalDistance = totalDistance;
            return this;
        }

        public Builder totalDuration(Double totalDuration) {
            this.totalDuration = totalDuration;
            return this;
        }

        public Builder distanceMatrix(List<List<Double>> distanceMatrix) {
            this.distanceMatrix = distanceMatrix;
            return this;
        }

        public OptimizedRoute build() {
            return new OptimizedRoute(optimizedSequence, totalDistance, totalDuration, distanceMatrix);
        }
    }

    @Override
    public String toString() {
        return "OptimizedRoute{" +
                "optimizedSequence=" + optimizedSequence +
                ", totalDistance=" + totalDistance +
                ", totalDuration=" + totalDuration +
                ", distanceMatrix=" + distanceMatrix +
                '}';
    }
}