// entity/Vehicle.java
package com.fleet_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String licensePlate;

    private String model;
    private Double capacity; // in tons
    private Double currentLatitude;
    private Double currentLongitude;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    private Integer maintenanceDueKm;
    private Integer currentOdometer;
    private LocalDateTime lastMaintenanceDate;

    @Column(nullable = false)
    private Boolean isAvailable = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

