// entity/DeliveryTask.java
package com.fleet.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_tasks")
@Data
public class DeliveryTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String deliveryAddress;
    private Double latitude;
    private Double longitude;

    private LocalDateTime timeWindowStart;
    private LocalDateTime timeWindowEnd;

    private Integer sequenceOrder;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @ManyToOne
    @JoinColumn(name = "assigned_vehicle_id")
    private Vehicle assignedVehicle;

    @ManyToOne
    @JoinColumn(name = "assigned_driver_id")
    private Driver assignedDriver;

    private Double packageWeight;
    private LocalDateTime actualDeliveryTime;
    private String notes;

    @PrePersist
    protected void onCreate() {
        status = DeliveryStatus.UNASSIGNED;
    }
}

