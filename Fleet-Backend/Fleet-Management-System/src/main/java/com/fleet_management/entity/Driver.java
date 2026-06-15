// entity/Driver.java
package com.fleet_management.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
//driver entity package
@Entity
@Table(name = "drivers")
@Data
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String licenseNumber;

    private LocalDate licenseExpiryDate;
    private Integer shiftHoursToday = 0;
    private Integer maxShiftHours = 8;

    @Enumerated(EnumType.STRING)
    private DriverStatus status;

    @OneToOne
    @JoinColumn(name = "assigned_vehicle_id")
    private Vehicle assignedVehicle;

    private String phoneNumber;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = DriverStatus.AVAILABLE;
    }

    public void setStatus(DriverStatus driverStatus) {
    }
}

