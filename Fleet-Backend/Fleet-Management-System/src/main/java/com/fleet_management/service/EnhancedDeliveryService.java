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

    @Transactional
    public DeliveryTask updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus, Long vehicleId) {
        System.out.println("=== Updating Delivery Status ===");
        System.out.println("Delivery ID: " + deliveryId);
        System.out.println("New Status: " + newStatus);
        System.out.println("Vehicle ID: " + vehicleId);

        // Find delivery
        DeliveryTask task = deliveryTaskRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));

        System.out.println("Current Status: " + task.getStatus());
        System.out.println("Customer: " + task.getCustomerName());

        // Apply state transition
        try {
            DeliveryTask updatedTask = stateMachine.transition(task, newStatus, LocalDateTime.now());
            System.out.println("Status transition successful");

            // Add additional business logic
            if (newStatus == DeliveryStatus.DELIVERED) {
                updatedTask.setActualDeliveryTime(LocalDateTime.now());
                System.out.println("Delivery completed at: " + LocalDateTime.now());
            }

            DeliveryTask saved = deliveryTaskRepository.save(updatedTask);
            System.out.println("Delivery saved with status: " + saved.getStatus());
            return saved;

        } catch (Exception e) {
            System.err.println("Error in state transition: " + e.getMessage());
            throw new RuntimeException("Failed to update delivery status: " + e.getMessage());
        }
    }

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

    public List<DeliveryStatus> getPossibleNextStatuses(Long deliveryId) {
        DeliveryTask task = deliveryTaskRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));

        return stateMachine.getNextPossibleStatuses(task).stream().toList();
    }
}