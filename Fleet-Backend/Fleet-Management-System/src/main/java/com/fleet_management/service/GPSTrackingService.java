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
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GPSTrackingService {

    @Autowired
    private VehicleRepository vehicleRepository;

    private final Map<Long, Coordinate> vehicleLocations = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> lastLocationUpdates = new ConcurrentHashMap<>();
    private final Map<Long, Double> vehicleSpeeds = new ConcurrentHashMap<>();
    private final Map<Long, Coordinate> vehicleDestinations = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Mumbai area bounds
    private static final double MIN_LAT = 18.9000;
    private static final double MAX_LAT = 19.3000;
    private static final double MIN_LNG = 72.8000;
    private static final double MAX_LNG = 73.1000;

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

        System.out.println("📍 Vehicle " + vehicleId + " location updated to: " + latitude + ", " + longitude);
    }

    public void simulateLocationUpdate(Long vehicleId) {
        Coordinate current = vehicleLocations.get(vehicleId);
        Coordinate destination = vehicleDestinations.get(vehicleId);

        if (current == null) {
            // Start from Mumbai city center
            current = new Coordinate(19.0760, 72.8777);
            vehicleLocations.put(vehicleId, current);
        }

        if (destination == null) {
            // Generate random destination
            destination = generateRandomLocation();
            vehicleDestinations.put(vehicleId, destination);
            vehicleSpeeds.put(vehicleId, 30.0 + random.nextDouble() * 30); // 30-60 km/h
        }

        // Calculate new position moving towards destination
        Coordinate newLocation = moveTowards(current, destination, vehicleSpeeds.get(vehicleId));

        // Check if reached destination
        if (distanceBetween(newLocation, destination) < 0.1) {
            // Generate new random destination
            destination = generateRandomLocation();
            vehicleDestinations.put(vehicleId, destination);
            vehicleSpeeds.put(vehicleId, 30.0 + random.nextDouble() * 30);
        }

        updateVehicleLocation(vehicleId, newLocation.getLatitude(), newLocation.getLongitude());
    }

    private Coordinate moveTowards(Coordinate from, Coordinate to, double speed) {
        double distance = distanceBetween(from, to);
        double step = speed / 3600.0; // Move per second (assuming called every second)

        if (distance <= step) {
            return to;
        }

        double ratio = step / distance;
        double newLat = from.getLatitude() + (to.getLatitude() - from.getLatitude()) * ratio;
        double newLng = from.getLongitude() + (to.getLongitude() - from.getLongitude()) * ratio;

        // Add random GPS error (±0.001 degrees, about 100m)
        newLat += (random.nextDouble() - 0.5) * 0.002;
        newLng += (random.nextDouble() - 0.5) * 0.002;

        return new Coordinate(newLat, newLng);
    }

    private double distanceBetween(Coordinate a, Coordinate b) {
        return calculateHaversineDistance(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
    }

    private Coordinate generateRandomLocation() {
        double lat = MIN_LAT + random.nextDouble() * (MAX_LAT - MIN_LAT);
        double lng = MIN_LNG + random.nextDouble() * (MAX_LNG - MIN_LNG);
        return new Coordinate(lat, lng);
    }

    public Coordinate getVehicleLocation(Long vehicleId) {
        return vehicleLocations.get(vehicleId);
    }

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

    public boolean isNearDeliveryLocation(Long vehicleId, DeliveryTask delivery) {
        double distance = calculateDistanceToDelivery(vehicleId, delivery);
        return distance >= 0 && distance <= 0.5; // Within 500 meters
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public LocalDateTime getLastLocationUpdate(Long vehicleId) {
        return lastLocationUpdates.get(vehicleId);
    }

    public boolean isLocationStale(Long vehicleId) {
        LocalDateTime lastUpdate = lastLocationUpdates.get(vehicleId);
        if (lastUpdate == null) return true;
        return LocalDateTime.now().minusMinutes(5).isAfter(lastUpdate);
    }

    public Map<Long, Coordinate> getAllVehicleLocations() {
        return new ConcurrentHashMap<>(vehicleLocations);
    }

    public double getVehicleSpeed(Long vehicleId) {
        return vehicleSpeeds.getOrDefault(vehicleId, 0.0);
    }
}