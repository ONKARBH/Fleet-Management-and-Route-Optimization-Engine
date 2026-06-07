// src/main/java/com/fleet_management/service/GPSTrackingService.java
package com.fleet_management.service;

import com.fleet_management.dto.Coordinate;
import com.fleet_management.entity.DeliveryTask;
import com.fleet_management.entity.Vehicle;
import com.fleet_management.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GPSTrackingService {

    @Autowired
    private VehicleRepository vehicleRepository;

    // Store real-time locations
    private final Map<Long, Coordinate> vehicleLocations = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> lastLocationUpdates = new ConcurrentHashMap<>();

    /**
     * Update vehicle location
     */
    public void updateVehicleLocation(Long vehicleId, Double latitude, Double longitude) {
        Coordinate newLocation = new Coordinate(latitude, longitude);
        vehicleLocations.put(vehicleId, newLocation);
        lastLocationUpdates.put(vehicleId, LocalDateTime.now());

        // Update in database
        vehicleRepository.findById(vehicleId).ifPresent(vehicle -> {
            vehicle.setCurrentLatitude(latitude);
            vehicle.setCurrentLongitude(longitude);
            vehicleRepository.save(vehicle);
        });
    }

    /**
     * Get current vehicle location
     */
    public Coordinate getVehicleLocation(Long vehicleId) {
        return vehicleLocations.get(vehicleId);
    }

    /**
     * Calculate distance from vehicle to delivery location
     */
    public double calculateDistanceToDelivery(Long vehicleId, DeliveryTask delivery) {
        Coordinate vehicleLocation = vehicleLocations.get(vehicleId);
        if (vehicleLocation == null || delivery.getLatitude() == null) {
            return -1;
        }

        return calculateHaversineDistance(
                vehicleLocation.getLatitude(), vehicleLocation.getLongitude(),
                delivery.getLatitude(), delivery.getLongitude()
        );
    }

    /**
     * Check if vehicle is near delivery location (within 100 meters)
     */
    public boolean isNearDeliveryLocation(Long vehicleId, DeliveryTask delivery) {
        double distance = calculateDistanceToDelivery(vehicleId, delivery);
        return distance >= 0 && distance <= 0.1; // 100 meters in km
    }

    /**
     * Calculate distance using Haversine formula
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Get last location update time
     */
    public LocalDateTime getLastLocationUpdate(Long vehicleId) {
        return lastLocationUpdates.get(vehicleId);
    }

    /**
     * Check if location data is stale (no update in last 5 minutes)
     */
    public boolean isLocationStale(Long vehicleId) {
        LocalDateTime lastUpdate = lastLocationUpdates.get(vehicleId);
        if (lastUpdate == null) return true;
        return LocalDateTime.now().minusMinutes(5).isAfter(lastUpdate);
    }
}