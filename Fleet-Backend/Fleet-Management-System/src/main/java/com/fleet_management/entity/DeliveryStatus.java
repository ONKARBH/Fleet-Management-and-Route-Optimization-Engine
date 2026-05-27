// src/main/java/com/fleet_management/entity/DeliveryStatus.java
package com.fleet_management.entity;

public enum DeliveryStatus {
    UNASSIGNED("Unassigned"),
    DISPATCHED("Dispatched"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Delivered"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    private final String displayName;

    DeliveryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}