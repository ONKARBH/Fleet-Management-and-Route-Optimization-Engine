// src/main/java/com/fleet_management/entity/DriverStatus.java
package com.fleet_management.entity;

public enum DriverStatus {
    AVAILABLE("Available"),
    ON_DUTY("On Duty"),
    OFF_DUTY("Off Duty"),
    ON_LEAVE("On Leave");

    private final String displayName;

    DriverStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}