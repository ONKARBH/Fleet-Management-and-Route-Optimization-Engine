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

@RestController
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
    @PostMapping("/{driverId}/location")
    public ResponseEntity<Map<String, Object>> updateLocation(
            @PathVariable Long driverId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        // Get vehicle ID associated with driver
        // For now, assume vehicle ID is same as driver ID or fetch from driver
        Long vehicleId = driverId; // Replace with actual vehicle lookup

        gpsTrackingService.updateVehicleLocation(vehicleId, latitude, longitude);

        // Get next delivery
        DeliveryTask nextDelivery = getNextDeliveryForDriver(driverId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Location updated");
        response.put("timestamp", java.time.LocalDateTime.now());

        if (nextDelivery != null) {
            double distance = gpsTrackingService.calculateDistanceToDelivery(vehicleId, nextDelivery);
            response.put("nextDeliveryId", nextDelivery.getId());
            response.put("distanceToNextDelivery", distance);
            response.put("estimatedTimeMinutes", distance / 40 * 60); // 40 km/h average
        }

        // Check if location data is stale
        if (gpsTrackingService.isLocationStale(vehicleId)) {
            response.put("warning", "Location data is stale. Please check GPS signal.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get next delivery
     */
    @GetMapping("/{driverId}/next-delivery")
    public ResponseEntity<DeliveryTask> getNextDelivery(@PathVariable Long driverId) {
        DeliveryTask nextDelivery = getNextDeliveryForDriver(driverId);
        if (nextDelivery == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nextDelivery);
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