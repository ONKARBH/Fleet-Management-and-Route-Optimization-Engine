// src/main/java/com/fleet_management/controller/VehicleController.java
package com.fleet_management.controller;

import com.fleet_management.entity.Vehicle;
import com.fleet_management.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

    // Create a new vehicle
    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        try {
            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            return new ResponseEntity<>(savedVehicle, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all vehicles
    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return ResponseEntity.ok(vehicles);
    }

    // Get vehicle by ID
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update vehicle
    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        if (!vehicleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        vehicle.setId(id);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return ResponseEntity.ok(updatedVehicle);
    }

    // Delete vehicle
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        if (!vehicleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        vehicleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}