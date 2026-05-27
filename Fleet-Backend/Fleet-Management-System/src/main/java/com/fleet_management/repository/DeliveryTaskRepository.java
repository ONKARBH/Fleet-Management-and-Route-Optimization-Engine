// repository/DeliveryTaskRepository.java
package com.fleet.repository;

import com.fleet.entity.DeliveryTask;
import com.fleet.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {
    List<DeliveryTask> findByAssignedVehicleIdAndStatus(Long vehicleId, DeliveryStatus status);
    List<DeliveryTask> findByAssignedDriverId(Long driverId);
}