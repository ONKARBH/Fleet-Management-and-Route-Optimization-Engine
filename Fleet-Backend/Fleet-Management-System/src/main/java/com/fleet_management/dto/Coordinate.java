// src/main/java/com/fleet_management/dto/Coordinate.java
package com.fleet_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate {
    private Double latitude;
    private Double longitude;

    public Coordinate(double v, double v1) {
    }

    public String toQueryParam() {
        // OSRM uses longitude,latitude format
        return longitude + "," + latitude;
    }

    // Manual getters and setters in case Lombok is not working
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}