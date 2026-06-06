// src/main/java/com/fleet_management/exception/VehicleNotAvailableException.java
package com.fleet_management.exception;

public class VehicleNotAvailableException extends RuntimeException {
    //vehicle seen message
    public VehicleNotAvailableException(String message) {
        super(message);
    }
}