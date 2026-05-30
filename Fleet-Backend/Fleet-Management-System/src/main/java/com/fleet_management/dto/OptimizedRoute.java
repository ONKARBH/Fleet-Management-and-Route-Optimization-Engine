// dto/OptimizedRoute.java
package com.fleet_management.dto;

import com.fleet_management.entity.DeliveryTask;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OptimizedRoute {
    private List<DeliveryTask> optimizedSequence;
    private Double totalDistance;
    private Double totalDuration;
    private List<List<Double>> distanceMatrix;
}