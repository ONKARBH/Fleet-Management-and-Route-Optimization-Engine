// src/main/java/com/fleet_management/controller/DispatchController.java
package com.fleet_management.controller;

import com.fleet_management.dto.OptimizedRoute;
import com.fleet_management.service.DispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    @Autowired
    private DispatchService dispatchService;

    @PostMapping("/create-and-optimize")
    public Mono<ResponseEntity<OptimizedRoute>> createDispatchAndOptimize(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== DispatchController called ===");
            System.out.println("Request body: " + request);

            // Parse IDs safely
            Long vehicleId = ((Number) request.get("vehicleId")).longValue();
            Long driverId = ((Number) request.get("driverId")).longValue();

            @SuppressWarnings("unchecked")
            List<Integer> taskIdsList = (List<Integer>) request.get("deliveryTaskIds");
            List<Long> deliveryTaskIds = taskIdsList.stream()
                    .map(Integer::longValue)
                    .toList();

            System.out.println("Vehicle ID: " + vehicleId);
            System.out.println("Driver ID: " + driverId);
            System.out.println("Delivery IDs: " + deliveryTaskIds);

            return dispatchService.createDispatchAndOptimize(vehicleId, driverId, deliveryTaskIds)
                    .map(ResponseEntity::ok)
                    .onErrorResume(error -> {
                        System.err.println("Error: " + error.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                    });

        } catch (Exception e) {
            System.err.println("Request parsing error: " + e.getMessage());
            e.printStackTrace();
            return Mono.just(ResponseEntity.badRequest().build());
        }
    }
}