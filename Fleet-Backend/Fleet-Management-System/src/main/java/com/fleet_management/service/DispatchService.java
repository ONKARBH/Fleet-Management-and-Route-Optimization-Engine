// src/main/java/com/fleet_management/service/DispatchService.java
package com.fleet_management.service;

import com.fleet_management.dto.OptimizedRoute;
import com.fleet_management.entity.*;
import com.fleet_management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
public class DispatchService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private DeliveryTaskRepository deliveryTaskRepository;

    @Transactional
    public Mono<OptimizedRoute> createDispatchAndOptimize(Long vehicleId,
                                                          Long driverId,
                                                          List<Long> deliveryTaskIds) {
        try {
            System.out.println("=== DispatchService Started ===");

            // Get vehicle
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + vehicleId));
            System.out.println("Vehicle found: " + vehicle.getLicensePlate());

            // Get driver
            Driver driver = driverRepository.findById(driverId)
                    .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + driverId));
            System.out.println("Driver found: " + driver.getName());

            // Get deliveries
            List<DeliveryTask> deliveries = deliveryTaskRepository.findAllById(deliveryTaskIds);
            System.out.println("Found " + deliveries.size() + " deliveries");

            if (deliveries.isEmpty()) {
                throw new RuntimeException("No deliveries found for IDs: " + deliveryTaskIds);
            }

            // Create optimized route (simple version - no actual optimization)
            OptimizedRoute optimizedRoute = new OptimizedRoute();
            optimizedRoute.setOptimizedSequence(deliveries);

            // Calculate mock total distance
            double totalDistance = deliveries.size() * 15.0; // Mock: 15 km per delivery
            optimizedRoute.setTotalDistance(totalDistance);
            optimizedRoute.setTotalDuration(totalDistance / 40.0); // 40 km/h average speed
            optimizedRoute.setTotalFuelCost(totalDistance * 12.0); // ₹12 per km

            // Update deliveries with sequence and assign vehicle/driver
            for (int i = 0; i < deliveries.size(); i++) {
                DeliveryTask task = deliveries.get(i);
                task.setSequenceOrder(i + 1);
                task.setStatus(DeliveryStatus.DISPATCHED);
                task.setAssignedVehicle(vehicle);
                task.setAssignedDriver(driver);
                deliveryTaskRepository.save(task);
                System.out.println("Updated delivery " + task.getId() + " - Sequence: " + (i+1) + ", Status: DISPATCHED");
            }

            // Update vehicle status
            vehicle.setStatus(VehicleStatus.ON_ROUTE);
            vehicleRepository.save(vehicle);
            System.out.println("Vehicle status updated to ON_ROUTE");

            // Update driver status
            driver.setStatus(DriverStatus.ON_DUTY);
            driverRepository.save(driver);
            System.out.println("Driver status updated to ON_DUTY");

            System.out.println("=== Dispatch completed successfully ===");
            return Mono.just(optimizedRoute);

        } catch (Exception e) {
            System.err.println("Error in dispatch: " + e.getMessage());
            e.printStackTrace();
            return Mono.error(new RuntimeException("Dispatch failed: " + e.getMessage()));
        }
    }
}