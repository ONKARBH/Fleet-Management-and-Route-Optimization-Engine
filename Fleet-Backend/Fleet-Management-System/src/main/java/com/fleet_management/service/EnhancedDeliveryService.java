// src/main/java/com/fleet_management/service/EnhancedDeliveryService.java
package com.fleet_management.service;

import com.fleet_management.entity.DeliveryStatus;
import com.fleet_management.entity.DeliveryTask;
import com.fleet_management.exception.DeliveryNotFoundException;
import com.fleet_management.repository.DeliveryTaskRepository;
import com.fleet_management.state.DeliveryStateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnhancedDeliveryService {

    @Autowired
    private DeliveryTaskRepository deliveryTaskRepository;

    @Autowired
    private DeliveryStateMachine stateMachine;

    @Autowired
    private GPSTrackingService gpsTrackingService;

    /**
     * Update delivery status with state machine validation
     */
    @Transactional
    public DeliveryTask updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus, Long vehicleId) {
        DeliveryTask task = deliveryTaskRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));

        // Apply state transition
        DeliveryTask updatedTask = stateMachine.transition(task, newStatus, LocalDateTime.now());

        // Additional business rules
        if (newStatus == DeliveryStatus.DELIVERED) {
            // Verify vehicle was near delivery location
            if (vehicleId != null && !gpsTrackingService.isNearDeliveryLocation(vehicleId, task)) {
                throw new RuntimeException("Cannot mark as delivered: Vehicle not near delivery location");
            }
        }

        if (newStatus == DeliveryStatus.IN_TRANSIT) {
            // Check if there's a previous undelivered delivery
            List<DeliveryTask> pendingDeliveries = deliveryTaskRepository
                    .findByAssignedVehicleIdAndStatus(vehicleId, DeliveryStatus.DISPATCHED);

            if (!pendingDeliveries.isEmpty() && pendingDeliveries.get(0).getId() != deliveryId) {
                throw new RuntimeException("Please complete deliveries in order. Next delivery is: "
                        + pendingDeliveries.get(0).getId());
            }
        }

        return deliveryTaskRepository.save(updatedTask);
    }

    /**
     * Cancel a delivery
     */
    @Transactional
    public DeliveryTask cancelDelivery(Long deliveryId, String reason) {
        DeliveryTask task = deliveryTaskRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));

        if (!stateMachine.canCancel(task)) {
            throw new RuntimeException("Cannot cancel delivery in status: " + task.getStatus());
        }

        task.setStatus(DeliveryStatus.CANCELLED);
        task.setNotes("Cancelled: " + reason);

        return deliveryTaskRepository.save(task);
    }

    /**
     * Retry a failed delivery
     */
    @Transactional
    public DeliveryTask retryDelivery(Long deliveryId) {
        DeliveryTask task = deliveryTaskRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));

        if (!stateMachine.canRetry(task)) {
            throw new RuntimeException("Can only retry failed deliveries");
        }

        task.setStatus(DeliveryStatus.DISPATCHED);
        task.setNotes("Retrying delivery after failure");

        return deliveryTaskRepository.save(task);
    }

    /**
     * Get possible next statuses
     */
    public List<DeliveryStatus> getPossibleNextStatuses(Long deliveryId) {
        DeliveryTask task = deliveryTaskRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));

        return stateMachine.getNextPossibleStatuses(task).stream().toList();
    }
}