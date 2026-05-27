// src/main/java/com/fleet_management/entity/VehicleStatus.java
package com.fleet_management.entity;

public enum VehicleStatus {
    AVAILABLE("Available"),
    ON_ROUTE("On Route"),
    MAINTENANCE("Under Maintenance"),
    OUT_OF_SERVICE("Out of Service");

    private final String displayName;

    VehicleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}