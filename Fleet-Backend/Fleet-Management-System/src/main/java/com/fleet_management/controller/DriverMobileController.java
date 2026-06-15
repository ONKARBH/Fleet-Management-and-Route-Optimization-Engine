// src/main/java/com/fleet_management/controller/DriverMobileController.java
package com.fleet_management.controller;

import com.fleet_management.dto.Coordinate;
import com.fleet_management.entity.DeliveryStatus;
import com.fleet_management.entity.DeliveryTask;
import com.fleet_management.repository.DeliveryTaskRepository;
import com.fleet_management.service.EnhancedDeliveryService;
import com.fleet_management.service.GPSTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequestMapping("/api/driver/mobile")
public class DriverMobileController {

    @Autowired
    private DeliveryTaskRepository deliveryTaskRepository;

    @Autowired
    private EnhancedDeliveryService deliveryService;

    @Autowired
    private GPSTrackingService gpsTrackingService;

    /**
     * Get today's optimized route for driver
     */
    @GetMapping("/{driverId}/today-route")
    public ResponseEntity<List<DeliveryTask>> getTodayRoute(@PathVariable Long driverId) {
        List<DeliveryTask> tasks = deliveryTaskRepository.findByAssignedDriverId(driverId);
        tasks.sort((t1, t2) -> t1.getSequenceOrder().compareTo(t2.getSequenceOrder()));
        return ResponseEntity.ok(tasks);
    }

    /**
     * Update delivery status with validation
     */
    @PutMapping("/delivery/{deliveryId}/status")
    public ResponseEntity<DeliveryTask> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @RequestParam DeliveryStatus newStatus,
            @RequestParam(required = false) Long vehicleId) {

        DeliveryTask updatedTask = deliveryService.updateDeliveryStatus(deliveryId, newStatus, vehicleId);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Update GPS location
     */
    // Add to DriverMobileController.java

    @PostMapping("/{driverId}/simulate-location")
    public ResponseEntity<Map<String, Object>> simulateLocationUpdate(
            @PathVariable Long driverId,
            @RequestParam(required = false, defaultValue = "true") boolean continuous) {

        Long vehicleId = driverId;

        if (continuous) {
            // Start continuous simulation
            startContinuousSimulation(vehicleId);
        } else {
            // Single update each time
            gpsTrackingService.simulateLocationUpdate(vehicleId);
        }

        Coordinate location = gpsTrackingService.getVehicleLocation(vehicleId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Location simulated");
        response.put("vehicleId", vehicleId);
        response.put("latitude", location != null ? location.getLatitude() : null);
        response.put("longitude", location != null ? location.getLongitude() : null);
        response.put("speed", gpsTrackingService.getVehicleSpeed(vehicleId));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{driverId}/stop-simulation")
    public ResponseEntity<Map<String, String>> stopSimulation(@PathVariable Long driverId) {
        stopContinuousSimulation(driverId);
        return ResponseEntity.ok(Map.of("status", "Simulation stopped"));
    }

    @GetMapping("/{driverId}/current-location")
    public ResponseEntity<Map<String, Object>> getCurrentLocation(@PathVariable Long driverId) {
        Long vehicleId = driverId;
        Coordinate location = gpsTrackingService.getVehicleLocation(vehicleId);

        Map<String, Object> response = new HashMap<>();
        response.put("vehicleId", vehicleId);
        response.put("latitude", location != null ? location.getLatitude() : null);
        response.put("longitude", location != null ? location.getLongitude() : null);
        response.put("speed", gpsTrackingService.getVehicleSpeed(vehicleId));
        response.put("lastUpdate", gpsTrackingService.getLastLocationUpdate(vehicleId));

        return ResponseEntity.ok(response);
    }

    // Background simulation for continuous updates
    private final Map<Long, Thread> simulationThreads = new ConcurrentHashMap<>();

    private void startContinuousSimulation(Long vehicleId) {
        // Stop existing simulation if any
        stopContinuousSimulation(vehicleId);

        Thread simulationThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    gpsTrackingService.simulateLocationUpdate(vehicleId);
                    Thread.sleep(2000); // Update every 2 seconds
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        simulationThread.setDaemon(true);
        simulationThread.start();
        simulationThreads.put(vehicleId, simulationThread);
    }

    private void stopContinuousSimulation(Long vehicleId) {
        Thread existingThread = simulationThreads.remove(vehicleId);
        if (existingThread != null) {
            existingThread.interrupt();
        }
    }

    /**
     * Cancel a delivery
     */
    @PostMapping("/delivery/{deliveryId}/cancel")
    public ResponseEntity<DeliveryTask> cancelDelivery(
            @PathVariable Long deliveryId,
            @RequestParam String reason) {

        DeliveryTask cancelledTask = deliveryService.cancelDelivery(deliveryId, reason);
        return ResponseEntity.ok(cancelledTask);
    }

    /**
     * Retry failed delivery
     */
    @PostMapping("/delivery/{deliveryId}/retry")
    public ResponseEntity<DeliveryTask> retryDelivery(@PathVariable Long deliveryId) {
        DeliveryTask retriedTask = deliveryService.retryDelivery(deliveryId);
        return ResponseEntity.ok(retriedTask);
    }

    /**
     * Get possible next statuses
     */
    @GetMapping("/delivery/{deliveryId}/possible-statuses")
    public ResponseEntity<List<DeliveryStatus>> getPossibleStatuses(@PathVariable Long deliveryId) {
        List<DeliveryStatus> statuses = deliveryService.getPossibleNextStatuses(deliveryId);
        return ResponseEntity.ok(statuses);
    }

    /**
     * Helper method to get next delivery for driver
     */
    private DeliveryTask getNextDeliveryForDriver(Long driverId) {
        List<DeliveryTask> tasks = deliveryTaskRepository.findByAssignedDriverId(driverId);
        return tasks.stream()
                .filter(t -> t.getStatus() == DeliveryStatus.DISPATCHED ||
                        t.getStatus() == DeliveryStatus.IN_TRANSIT)
                .findFirst()
                .orElse(null);
    }
}