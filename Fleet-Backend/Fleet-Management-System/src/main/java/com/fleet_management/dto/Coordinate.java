// dto/Coordinate.java
package com.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate {
    private Double latitude;
    private Double longitude;

    public String toQueryParam() {
        return longitude + "," + latitude; // OSRM uses lng,lat format
    }
}