// repository/DriverRepository.java
package com.fleet_management.repository;

import com.fleet_management.entity.Driver;
import com.fleet_management.entity.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByStatus(DriverStatus status);
    List<Driver> findByAssignedVehicleId(Long vehicleId);
}