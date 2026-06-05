// src/main/java/com/fleet_management/dto/DistanceMatrixRequest.java
package com.fleet_management.dto;

import lombok.Data;
import java.util.List;

@Data
public class DistanceMatrixRequest {
    private List<Coordinate> waypoints;
    private Long vehicleId;

    public List<Coordinate> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Coordinate> waypoints) {
        this.waypoints = waypoints;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
}