// entity/Route.java
package com.fleet.entity;

import com.fleet_management.entity.Driver;
import com.fleet_management.entity.Vehicle;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "routes")
@Data
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double totalDistance;
    private Double totalDuration;

    @OneToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @OneToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    private RouteStatus status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = RouteStatus.PLANNED;
    }
}

