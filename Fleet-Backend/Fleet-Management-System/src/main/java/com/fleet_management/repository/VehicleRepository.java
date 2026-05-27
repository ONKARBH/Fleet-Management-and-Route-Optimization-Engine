// src/main/java/com/fleet_management/repository/VehicleRepository.java
package com.fleet_management.repository;

import com.fleet_management.entity.Vehicle;
import com.fleet_management.entity.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByStatus(VehicleStatus status);
    List<Vehicle> findByIsAvailableTrue();
    boolean existsByLicensePlate(String licensePlate);
}