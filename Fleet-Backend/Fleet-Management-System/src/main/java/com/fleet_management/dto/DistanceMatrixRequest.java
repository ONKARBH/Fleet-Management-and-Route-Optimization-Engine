// dto/DistanceMatrixRequest.java
package com.fleet_management.dto;

import lombok.Data;
import java.util.List;

@Data
public class DistanceMatrixRequest {
    private List<com.fleet.dto.Coordinate> waypoints;
    private Long vehicleId;
}