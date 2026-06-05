// src/main/java/com/fleet_management/dto/OptimizedRoute.java
package com.fleet_management.dto;

import com.fleet_management.entity.DeliveryTask;
import java.util.List;

public class OptimizedRoute {
    private List<DeliveryTask> optimizedSequence;
    private List<Integer> optimizedOrderIndices;
    private Double totalDistance;
    private Double totalDuration;
    private List<List<Double>> distanceMatrix;
    private List<List<Double>> durationMatrix;
    private Double totalFuelCost;
    private Double totalTimeHours;

    // No-args constructor (REQUIRED for simple instantiation)
    public OptimizedRoute() {
    }

    // All-args constructor
    public OptimizedRoute(List<DeliveryTask> optimizedSequence, List<Integer> optimizedOrderIndices,
                          Double totalDistance, Double totalDuration, List<List<Double>> distanceMatrix,
                          List<List<Double>> durationMatrix, Double totalFuelCost, Double totalTimeHours) {
        this.optimizedSequence = optimizedSequence;
        this.optimizedOrderIndices = optimizedOrderIndices;
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
        this.distanceMatrix = distanceMatrix;
        this.durationMatrix = durationMatrix;
        this.totalFuelCost = totalFuelCost;
        this.totalTimeHours = totalTimeHours;
    }

    // Builder method
    public static Builder builder() {
        return new Builder();
    }

    // Builder class
    public static class Builder {
        private List<DeliveryTask> optimizedSequence;
        private List<Integer> optimizedOrderIndices;
        private Double totalDistance;
        private Double totalDuration;
        private List<List<Double>> distanceMatrix;
        private List<List<Double>> durationMatrix;
        private Double totalFuelCost;
        private Double totalTimeHours;

        public Builder optimizedSequence(List<DeliveryTask> optimizedSequence) {
            this.optimizedSequence = optimizedSequence;
            return this;
        }

        public Builder optimizedOrderIndices(List<Integer> optimizedOrderIndices) {
            this.optimizedOrderIndices = optimizedOrderIndices;
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

        public Builder durationMatrix(List<List<Double>> durationMatrix) {
            this.durationMatrix = durationMatrix;
            return this;
        }

        public Builder totalFuelCost(Double totalFuelCost) {
            this.totalFuelCost = totalFuelCost;
            return this;
        }

        public Builder totalTimeHours(Double totalTimeHours) {
            this.totalTimeHours = totalTimeHours;
            return this;
        }

        public OptimizedRoute build() {
            return new OptimizedRoute(optimizedSequence, optimizedOrderIndices, totalDistance,
                    totalDuration, distanceMatrix, durationMatrix, totalFuelCost, totalTimeHours);
        }
    }

    // Getters and Setters
    public List<DeliveryTask> getOptimizedSequence() {
        return optimizedSequence;
    }

    public void setOptimizedSequence(List<DeliveryTask> optimizedSequence) {
        this.optimizedSequence = optimizedSequence;
    }

    public List<Integer> getOptimizedOrderIndices() {
        return optimizedOrderIndices;
    }

    public void setOptimizedOrderIndices(List<Integer> optimizedOrderIndices) {
        this.optimizedOrderIndices = optimizedOrderIndices;
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

    public List<List<Double>> getDurationMatrix() {
        return durationMatrix;
    }

    public void setDurationMatrix(List<List<Double>> durationMatrix) {
        this.durationMatrix = durationMatrix;
    }

    public Double getTotalFuelCost() {
        return totalFuelCost;
    }

    public void setTotalFuelCost(Double totalFuelCost) {
        this.totalFuelCost = totalFuelCost;
    }

    public Double getTotalTimeHours() {
        return totalTimeHours;
    }

    public void setTotalTimeHours(Double totalTimeHours) {
        this.totalTimeHours = totalTimeHours;
    }

    // Helper method to calculate fuel cost
    public Double calculateFuelCost() {
        if (totalDistance != null) {
            return totalDistance * 12.0;
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return "OptimizedRoute{" +
                "optimizedSequence=" + optimizedSequence +
                ", totalDistance=" + totalDistance +
                ", totalDuration=" + totalDuration +
                ", totalFuelCost=" + totalFuelCost +
                '}';
    }
}