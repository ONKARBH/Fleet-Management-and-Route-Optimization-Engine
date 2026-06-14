// src/main/java/com/fleet_management/controller/DeliveryTaskController.java
package com.fleet_management.controller;

import com.fleet_management.entity.DeliveryTask;
import com.fleet_management.repository.DeliveryTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryTaskController {

    @Autowired
    private DeliveryTaskRepository deliveryTaskRepository;

    @PostMapping
    public ResponseEntity<DeliveryTask> createDelivery(@RequestBody DeliveryTask deliveryTask) {
        try {
            DeliveryTask savedTask = deliveryTaskRepository.save(deliveryTask);
            return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<DeliveryTask>> getAllDeliveries() {
        List<DeliveryTask> deliveries = deliveryTaskRepository.findAll();
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryTask> getDeliveryById(@PathVariable Long id) {
        return deliveryTaskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryTask> updateDelivery(@PathVariable Long id, @RequestBody DeliveryTask deliveryTask) {
        if (!deliveryTaskRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        deliveryTask.setId(id);
        DeliveryTask updatedTask = deliveryTaskRepository.save(deliveryTask);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long id) {
        if (!deliveryTaskRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        deliveryTaskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}