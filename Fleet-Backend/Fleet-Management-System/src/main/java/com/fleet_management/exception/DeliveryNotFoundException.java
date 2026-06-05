// src/main/java/com/fleet_management/exception/DeliveryNotFoundException.java
package com.fleet_management.exception;

public class DeliveryNotFoundException extends RuntimeException {

    public DeliveryNotFoundException(Long id) {
        super("Delivery not found with ID: " + id);
    }
}