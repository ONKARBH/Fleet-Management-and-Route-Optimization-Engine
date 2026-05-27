// src/main/java/com/fleet_management/entity/RouteStatus.java
package com.fleet_management.entity;

public enum RouteStatus {
    PLANNED("Planned"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    RouteStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}