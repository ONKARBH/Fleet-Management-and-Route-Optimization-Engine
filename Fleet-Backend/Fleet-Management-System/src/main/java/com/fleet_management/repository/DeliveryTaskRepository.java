// repository/DeliveryTaskRepository.java
package com.fleet_management.repository;

import com.fleet_management.entity.DeliveryTask;
import com.fleet_management.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {
    List<DeliveryTask> findByAssignedVehicleIdAndStatus(Long vehicleId, DeliveryStatus status);
    List<DeliveryTask> findByAssignedDriverId(Long driverId);
}