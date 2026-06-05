// src/main/java/com/fleet_management/repository/RouteRepository.java
package com.fleet_management.repository;

import com.fleet_management.entity.Route;
import com.fleet_management.entity.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    // Find routes by vehicle ID
    List<Route> findByVehicleId(Long vehicleId);

    // Find routes by driver ID
    List<Route> findByDriverId(Long driverId);

    // Find routes by status
    List<Route> findByStatus(RouteStatus status);

    // Find active routes for a vehicle
    @Query("SELECT r FROM Route r WHERE r.vehicle.id = :vehicleId AND r.status = :status")
    List<Route> findActiveRoutesByVehicle(@Param("vehicleId") Long vehicleId,
                                          @Param("status") RouteStatus status);

    // Find routes between dates
    List<Route> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // Find routes completed today
    @Query("SELECT r FROM Route r WHERE DATE(r.startTime) = CURRENT_DATE")
    List<Route> findTodaysRoutes();

    // Count active routes
    @Query("SELECT COUNT(r) FROM Route r WHERE r.status = :status")
    long countByStatus(@Param("status") RouteStatus status);
}